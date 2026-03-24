const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

// ─────────────────────────────────────────────────────────────
// TRIGGER 1: KTV vừa chuyển sang "Ran"
//   → quét toàn bộ HangCho và assign ngay
// ─────────────────────────────────────────────────────────────
exports.onKtvOnline = functions.firestore
  .document("NguoiDung/{ktvUid}")
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after  = change.after.data();

    // Chỉ xử lý khi KTV vừa chuyển sang Rảnh
    const vừaRảnh =
      after.vaiTro === "KTV" &&
      after.trangThai === "Ran" &&
      before.trangThai !== "Ran";

    if (!vừaRảnh) return null;

    const ktvUid = context.params.ktvUid;
    const ktvTen = after.hoTen || "";

    return xuLyHangCho(ktvUid, ktvTen, after.chuyenMon || []);
  });

// ─────────────────────────────────────────────────────────────
// TRIGGER 2: Ticket mới được tạo với trangThai = "HangCho"
//   → thử tìm KTV rảnh ngay lập tức
// ─────────────────────────────────────────────────────────────
exports.onTicketCreated = functions.firestore
  .document("YeuCauHoTro/{ticketId}")
  .onCreate(async (snap, context) => {
    const ticket = snap.data();
    const ticketId = context.params.ticketId;

    // Chỉ xử lý ticket mới vào HangCho (không có KTV)
    if (ticket.trangThai !== "HangCho" && ticket.trangThai !== "ChoXuLy") return null;
    if (ticket.ktvUid) return null; // đã có KTV rồi

    const sanPham = ticket.sanPham || "";
    const ktv = await timKtvRanh(sanPham);
    if (!ktv) return null; // không có KTV rảnh, giữ HangCho

    return assignKtvChoTicket(ticketId, ktv.uid, ktv.ten);
  });

// ─────────────────────────────────────────────────────────────
// TRIGGER 3: Scheduled - quét HangCho mỗi 1 phút
//   → đảm bảo không ticket nào bị bỏ sót
// ─────────────────────────────────────────────────────────────
exports.scheduledRouter = functions.pubsub
  .schedule("every 1 minutes")
  .onRun(async () => {
    const hangChoSnap = await db.collection("YeuCauHoTro")
      .where("trangThai", "==", "HangCho")
      .orderBy("thoiGianChoXuLy", "asc")
      .limit(20)
      .get();

    if (hangChoSnap.empty) return null;

    for (const doc of hangChoSnap.docs) {
      const ticket = doc.data();
      if (ticket.ktvUid) continue; // đã assign rồi

      const ktv = await timKtvRanh(ticket.sanPham || "");
      if (!ktv) break; // không còn KTV rảnh nào, dừng

      await assignKtvChoTicket(doc.id, ktv.uid, ktv.ten);
    }

    return null;
  });

// ─────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────

/**
 * Tìm KTV rảnh tốt nhất: ưu tiên chuyên môn + ít ticket nhất
 */
async function timKtvRanh(sanPham) {
  const snap = await db.collection("NguoiDung")
    .where("vaiTro", "==", "KTV")
    .where("trangThai", "==", "Ran")
    .get();

  if (snap.empty) return null;

  let best = null;
  let minTicket = Infinity;

  // Ưu tiên KTV có chuyên môn
  for (const doc of snap.docs) {
    const ktv = doc.data();
    const coChuyenMon = Array.isArray(ktv.chuyenMon) && ktv.chuyenMon.includes(sanPham);
    if (coChuyenMon && (ktv.soTicketDangXuLy || 0) < minTicket) {
      minTicket = ktv.soTicketDangXuLy || 0;
      best = { uid: doc.id, ten: ktv.hoTen || "" };
    }
  }

  // Fallback: KTV rảnh bất kỳ ít ticket nhất
  if (!best) {
    minTicket = Infinity;
    for (const doc of snap.docs) {
      const ktv = doc.data();
      if ((ktv.soTicketDangXuLy || 0) < minTicket) {
        minTicket = ktv.soTicketDangXuLy || 0;
        best = { uid: doc.id, ten: ktv.hoTen || "" };
      }
    }
  }

  return best;
}

/**
 * Assign KTV cho ticket + tăng counter (dùng transaction để tránh race condition)
 */
async function assignKtvChoTicket(ticketId, ktvUid, ktvTen) {
  const ticketRef = db.collection("YeuCauHoTro").document(ticketId);
  const ktvRef    = db.collection("NguoiDung").document(ktvUid);

  return db.runTransaction(async (tx) => {
    const ticketDoc = await tx.get(ticketRef);
    if (!ticketDoc.exists) return;

    // Double-check: ticket vẫn chưa có KTV
    const current = ticketDoc.data();
    if (current.ktvUid && current.ktvUid !== "") return;

    tx.update(ticketRef, {
      ktvUid:     ktvUid,
      ktvTen:     ktvTen,
      trangThai:  "ChoXuLy",
      capNhatLuc: admin.firestore.FieldValue.serverTimestamp(),
    });

    tx.update(ktvRef, {
      soTicketDangXuLy: admin.firestore.FieldValue.increment(1),
    });
  });
}

/**
 * Quét HangCho và assign cho KTV vừa online
 */
async function xuLyHangCho(ktvUid, ktvTen, chuyenMon) {
  const snap = await db.collection("YeuCauHoTro")
    .where("trangThai", "==", "HangCho")
    .orderBy("thoiGianChoXuLy", "asc")
    .limit(10)
    .get();

  if (snap.empty) return null;

  // Lấy số ticket hiện tại của KTV này
  const ktvDoc = await db.collection("NguoiDung").document(ktvUid).get();
  const soTicket = ktvDoc.data()?.soTicketDangXuLy || 0;
  const MAX_TICKET = 5; // giới hạn tối đa mỗi KTV

  if (soTicket >= MAX_TICKET) return null;

  let assigned = 0;
  const canTake = MAX_TICKET - soTicket;

  for (const doc of snap.docs) {
    if (assigned >= canTake) break;
    const ticket = doc.data();
    if (ticket.ktvUid) continue; // đã có KTV

    // Ưu tiên ticket phù hợp chuyên môn trước
    const phuHop = chuyenMon.length === 0 || chuyenMon.includes(ticket.sanPham || "");
    if (!phuHop && snap.docs.some(d => !d.data().ktvUid && chuyenMon.includes(d.data().sanPham))) {
      continue; // bỏ qua ticket không phù hợp nếu còn ticket phù hợp
    }

    await assignKtvChoTicket(doc.id, ktvUid, ktvTen);
    assigned++;
  }

  return null;
}
