package com.example.customercareproject.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.GoiDangKy;
import com.example.customercareproject.model.SanPham;
import com.example.customercareproject.ui.components.Material3TextField;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminGoiDangKyFragment extends Fragment {

    private RecyclerView rvGoiDangKy;
    private TextView tvEmpty;
    private GoiDangKyAdapter adapter;
    private final List<GoiDangKy> goiList = new ArrayList<>();
    private final List<String> docIdList = new ArrayList<>();

    private static final String[] TRANG_THAI_LABELS = {"Hoạt động", "Hết hạn", "Tạm dừng"};
    private static final String[] TRANG_THAI_VALUES = {
            GoiDangKy.TRANG_THAI_HOAT_DONG,
            GoiDangKy.TRANG_THAI_HET_HAN,
            GoiDangKy.TRANG_THAI_TAM_DUNG
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_goi_dang_ky, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvGoiDangKy = view.findViewById(R.id.rvGoiDangKy);
        tvEmpty = view.findViewById(R.id.tvEmptyGoi);

        adapter = new GoiDangKyAdapter(goiList, docIdList, new GoiDangKyAdapter.Listener() {
            @Override
            public void onSua(GoiDangKy goi, String docId) {
                hienThiMenuSua(goi, docId);
            }
            @Override
            public void onXoa(GoiDangKy goi, String docId) {
                xacNhanXoa(goi, docId);
            }
        });
        rvGoiDangKy.setLayoutManager(new LinearLayoutManager(getContext()));
        rvGoiDangKy.setAdapter(adapter);

        view.findViewById(R.id.btnThemGoi).setOnClickListener(v -> hienThiDialogThem(null, null));

        // Nút reset seed data (dùng khi database cũ có tên SP sai)
        View btnReset = view.findViewById(R.id.btnResetSeed);
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Reset dữ liệu mẫu")
                        .setMessage("Xóa toàn bộ gói đăng ký hiện tại và tạo lại dữ liệu mẫu với đúng tên sản phẩm?\n\nChỉ dùng khi database có dữ liệu cũ sai.")
                        .setPositiveButton("Reset", (d, w) -> {
                            com.example.customercareproject.utils.DataSeeder.reseedGoiDangKy(
                                    com.google.firebase.firestore.FirebaseFirestore.getInstance(),
                                    () -> {
                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(() -> {
                                                android.widget.Toast.makeText(getContext(),
                                                        "Đã reset dữ liệu mẫu", android.widget.Toast.LENGTH_SHORT).show();
                                                taiDanhSach();
                                            });
                                        }
                                    });
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }

        taiDanhSach();
    }

    private void taiDanhSach() {
        if (getContext() == null) return;
        FirebaseFirestore.getInstance().collection("GoiDangKy")
                .orderBy("tenCongTy")
                .get()
                .addOnSuccessListener(snap -> {
                    if (getContext() == null) return;
                    goiList.clear();
                    docIdList.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        GoiDangKy g = doc.toObject(GoiDangKy.class);
                        goiList.add(g);
                        docIdList.add(doc.getId());
                    }
                    adapter.capNhat(new ArrayList<>(goiList), new ArrayList<>(docIdList));
                    tvEmpty.setVisibility(goiList.isEmpty() ? View.VISIBLE : View.GONE);
                    rvGoiDangKy.setVisibility(goiList.isEmpty() ? View.GONE : View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    if (getContext() == null) return;
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }

    private void hienThiDialogThem(@Nullable GoiDangKy goiCu, @Nullable String docId) {
        if (getContext() == null) return;
        boolean isEdit = (goiCu != null);

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_goi_dang_ky, null);

        com.example.customercareproject.ui.components.Material3TextField edtMst = dialogView.findViewById(R.id.edtMaSoThue);
        com.example.customercareproject.ui.components.Material3TextField edtTen = dialogView.findViewById(R.id.edtTenCongTy);
        RadioButton rbChinhThuc = dialogView.findViewById(R.id.rbChinhThuc);
        RadioButton rbDungThu   = dialogView.findViewById(R.id.rbDungThu);
        Spinner spinnerTs       = dialogView.findViewById(R.id.spinnerTrangThai);

        CheckBox[] checkBoxes = {
                dialogView.findViewById(R.id.cbEcus5),
                dialogView.findViewById(R.id.cbEinvoice),
                dialogView.findViewById(R.id.cbEtax),
                dialogView.findViewById(R.id.cbEbh),
                dialogView.findViewById(R.id.cbCloudoffice),
                dialogView.findViewById(R.id.cbTruepos)
        };

        // Spinner trạng thái
        ArrayAdapter<String> tsAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, TRANG_THAI_LABELS);
        tsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTs.setAdapter(tsAdapter);

        // Điền dữ liệu nếu đang sửa
        if (isEdit) {
            edtMst.setText(goiCu.getMaSoThue());
            edtMst.setEnabled(false); // MST không đổi khi sửa
            edtTen.setText(goiCu.getTenCongTy());

            if (GoiDangKy.LOAI_GOI_DUNG_THU.equals(goiCu.getLoaiGoi())) {
                rbDungThu.setChecked(true);
            } else {
                rbChinhThuc.setChecked(true);
            }

            List<String> spDangKy = goiCu.getSanPhamDangKy();
            if (spDangKy != null) {
                String[] allSp = SanPham.DANH_SACH;
                for (int i = 0; i < allSp.length; i++) {
                    checkBoxes[i].setChecked(spDangKy.contains(allSp[i]));
                }
            }

            // Set trạng thái spinner
            String ts = goiCu.getTrangThai();
            for (int i = 0; i < TRANG_THAI_VALUES.length; i++) {
                if (TRANG_THAI_VALUES[i].equals(ts)) {
                    spinnerTs.setSelection(i);
                    break;
                }
            }
        }

        new AlertDialog.Builder(getContext())
                .setTitle(isEdit ? "Sửa gói đăng ký" : "Thêm gói đăng ký")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String mst = edtMst.getText() != null ? edtMst.getText().toString().trim() : "";
                    String ten = edtTen.getText() != null ? edtTen.getText().toString().trim() : "";

                    if (mst.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập mã số thuế", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (ten.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập tên công ty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> spChon = new ArrayList<>();
                    String[] allSp = SanPham.DANH_SACH;
                    for (int i = 0; i < checkBoxes.length; i++) {
                        if (checkBoxes[i].isChecked()) spChon.add(allSp[i]);
                    }
                    if (spChon.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng chọn ít nhất 1 sản phẩm", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String loaiGoi = rbDungThu.isChecked()
                            ? GoiDangKy.LOAI_GOI_DUNG_THU
                            : GoiDangKy.LOAI_GOI_CHINH_THUC;
                    String trangThai = TRANG_THAI_VALUES[spinnerTs.getSelectedItemPosition()];

                    luuGoiDangKy(mst, ten, spChon, loaiGoi, trangThai, isEdit ? docId : null);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void luuGoiDangKy(String mst, String ten, List<String> sanPham,
                               String loaiGoi, String trangThai, @Nullable String docId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("maSoThue", mst);
        data.put("tenCongTy", ten);
        data.put("sanPhamDangKy", sanPham);
        data.put("loaiGoi", loaiGoi);
        data.put("trangThai", trangThai);
        data.put("ngayHetHan", null);

        if (docId != null) {
            // Sửa
            data.put("ngayDangKy", Timestamp.now()); // giữ nguyên nếu muốn, nhưng đơn giản hóa
            db.collection("GoiDangKy").document(docId).update(data)
                    .addOnSuccessListener(v -> {
                        if (getContext() == null) return;
                        Toast.makeText(getContext(), "Đã cập nhật gói", Toast.LENGTH_SHORT).show();
                        taiDanhSach();
                    })
                    .addOnFailureListener(e -> {
                        if (getContext() == null) return;
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Thêm mới — dùng MST làm document ID
            data.put("ngayDangKy", Timestamp.now());
            db.collection("GoiDangKy").document(mst).set(data)
                    .addOnSuccessListener(v -> {
                        if (getContext() == null) return;
                        Toast.makeText(getContext(), "Đã thêm gói mới", Toast.LENGTH_SHORT).show();
                        taiDanhSach();
                    })
                    .addOnFailureListener(e -> {
                        if (getContext() == null) return;
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /** Menu chọn hành động khi nhấn "Sửa" trên gói đang có */
    private void hienThiMenuSua(GoiDangKy goi, String docId) {
        if (getContext() == null) return;
        String[] options = {"Thêm sản phẩm", "Gỡ sản phẩm", "Đổi thông tin & trạng thái"};
        new AlertDialog.Builder(getContext())
                .setTitle(goi.getTenCongTy())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: hienThiDialogThemSanPham(goi, docId); break;
                        case 1: hienThiDialogGoSanPham(goi, docId); break;
                        case 2: hienThiDialogDoiThongTin(goi, docId); break;
                    }
                })
                .show();
    }

    /** Chỉ hiện các sản phẩm CHƯA có trong gói để chọn thêm */
    private void hienThiDialogThemSanPham(GoiDangKy goi, String docId) {
        if (getContext() == null) return;

        List<String> tatCa = new ArrayList<>(java.util.Arrays.asList(SanPham.DANH_SACH));
        List<String> daDangKy = goi.getSanPhamDangKy();
        List<String> chuaCo = new ArrayList<>();
        for (String sp : tatCa) {
            if (!daDangKy.contains(sp)) chuaCo.add(sp);
        }

        if (chuaCo.isEmpty()) {
            Toast.makeText(getContext(), "Công ty đã đăng ký tất cả sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chọn loại trước khi chọn sản phẩm
        String[] loaiOptions = {"Chính thức (đã ký hợp đồng)", "Dùng thử"};
        new AlertDialog.Builder(getContext())
                .setTitle("Loại đăng ký")
                .setItems(loaiOptions, (d, loaiIdx) -> {
                    boolean isChinhThuc = (loaiIdx == 0);
                    boolean[] checked = new boolean[chuaCo.size()];
                    String[] items = chuaCo.toArray(new String[0]);

                    new AlertDialog.Builder(getContext())
                            .setTitle("Thêm sản phẩm " + (isChinhThuc ? "chính thức" : "dùng thử"))
                            .setMultiChoiceItems(items, checked,
                                    (d2, which, isChecked) -> checked[which] = isChecked)
                            .setPositiveButton("Thêm", (d2, w) -> {
                                List<String> spThemMoi = new ArrayList<>();
                                for (int i = 0; i < checked.length; i++) {
                                    if (checked[i]) spThemMoi.add(chuaCo.get(i));
                                }
                                if (spThemMoi.isEmpty()) {
                                    Toast.makeText(getContext(), "Chưa chọn sản phẩm nào", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                List<String> chinhThucMoi = new ArrayList<>(goi.getSanPhamChinhThuc());
                                List<String> dungThuMoi   = new ArrayList<>(goi.getSanPhamDungThu());

                                if (isChinhThuc) chinhThucMoi.addAll(spThemMoi);
                                else             dungThuMoi.addAll(spThemMoi);

                                capNhatHaiList(docId, chinhThucMoi, dungThuMoi,
                                        "Đã thêm " + spThemMoi.size() + " sản phẩm");
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                })
                .show();
    }

    /** Chỉ hiện các sản phẩm ĐANG có trong gói để bỏ bớt */
    private void hienThiDialogGoSanPham(GoiDangKy goi, String docId) {
        if (getContext() == null) return;

        List<String> chinhThuc = goi.getSanPhamChinhThuc();
        List<String> dungThu   = goi.getSanPhamDungThu();

        if (chinhThuc.isEmpty() && dungThu.isEmpty()) {
            Toast.makeText(getContext(), "Gói chưa có sản phẩm nào", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiện tất cả SP đang có, kèm nhãn loại
        List<String> tatCaSp = new ArrayList<>();
        List<String> tatCaLabel = new ArrayList<>();
        for (String sp : chinhThuc) {
            tatCaSp.add(sp);
            tatCaLabel.add(sp + " [Chính thức]");
        }
        for (String sp : dungThu) {
            tatCaSp.add(sp);
            tatCaLabel.add(sp + " [Dùng thử]");
        }

        boolean[] checked = new boolean[tatCaSp.size()];
        String[] items = tatCaLabel.toArray(new String[0]);

        new AlertDialog.Builder(getContext())
                .setTitle("Gỡ sản phẩm khỏi " + goi.getTenCongTy())
                .setMessage("Tick vào sản phẩm muốn gỡ:")
                .setMultiChoiceItems(items, checked,
                        (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("Gỡ đã chọn", (dialog, w) -> {
                    List<String> spGo = new ArrayList<>();
                    for (int i = 0; i < checked.length; i++) {
                        if (checked[i]) spGo.add(tatCaSp.get(i));
                    }
                    if (spGo.isEmpty()) {
                        Toast.makeText(getContext(), "Chưa chọn sản phẩm nào để gỡ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<String> chinhThucMoi = new ArrayList<>(chinhThuc);
                    List<String> dungThuMoi   = new ArrayList<>(dungThu);
                    chinhThucMoi.removeAll(spGo);
                    dungThuMoi.removeAll(spGo);

                    if (chinhThucMoi.isEmpty() && dungThuMoi.isEmpty()) {
                        Toast.makeText(getContext(), "Phải giữ ít nhất 1 sản phẩm", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    capNhatHaiList(docId, chinhThucMoi, dungThuMoi,
                            "Đã gỡ " + spGo.size() + " sản phẩm");
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /** Cập nhật cả 2 list + union lên Firestore */
    private void capNhatHaiList(String docId, List<String> chinhThuc, List<String> dungThu, String msg) {
        List<String> union = new ArrayList<>(chinhThuc);
        for (String sp : dungThu) {
            if (!union.contains(sp)) union.add(sp);
        }
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("sanPhamChinhThuc", chinhThuc);
        data.put("sanPhamDungThu", dungThu);
        data.put("sanPhamDangKy", union);

        FirebaseFirestore.getInstance().collection("GoiDangKy").document(docId)
                .update(data)
                .addOnSuccessListener(v -> {
                    if (getContext() == null) return;
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    taiDanhSach();
                })
                .addOnFailureListener(e -> {
                    if (getContext() == null) return;
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /** Dialog đổi thông tin cơ bản: tên, loại gói, trạng thái (không đụng sản phẩm) */
    private void hienThiDialogDoiThongTin(GoiDangKy goi, String docId) {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_goi_dang_ky, null);

        com.example.customercareproject.ui.components.Material3TextField edtMst = dialogView.findViewById(R.id.edtMaSoThue);
        com.example.customercareproject.ui.components.Material3TextField edtTen = dialogView.findViewById(R.id.edtTenCongTy);
        RadioButton rbChinhThuc  = dialogView.findViewById(R.id.rbChinhThuc);
        RadioButton rbDungThu    = dialogView.findViewById(R.id.rbDungThu);
        Spinner spinnerTs        = dialogView.findViewById(R.id.spinnerTrangThai);

        // Ẩn phần chọn sản phẩm — không cần trong dialog này
        dialogView.findViewById(R.id.cbEcus5).setVisibility(View.GONE);
        dialogView.findViewById(R.id.cbEinvoice).setVisibility(View.GONE);
        dialogView.findViewById(R.id.cbEtax).setVisibility(View.GONE);
        dialogView.findViewById(R.id.cbEbh).setVisibility(View.GONE);
        dialogView.findViewById(R.id.cbCloudoffice).setVisibility(View.GONE);
        dialogView.findViewById(R.id.cbTruepos).setVisibility(View.GONE);

        ArrayAdapter<String> tsAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, TRANG_THAI_LABELS);
        tsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTs.setAdapter(tsAdapter);

        edtMst.setText(goi.getMaSoThue());
        edtMst.setEnabled(false);
        edtTen.setText(goi.getTenCongTy());

        if (GoiDangKy.LOAI_GOI_DUNG_THU.equals(goi.getLoaiGoi())) rbDungThu.setChecked(true);
        else rbChinhThuc.setChecked(true);

        String ts = goi.getTrangThai();
        for (int i = 0; i < TRANG_THAI_VALUES.length; i++) {
            if (TRANG_THAI_VALUES[i].equals(ts)) { spinnerTs.setSelection(i); break; }
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Đổi thông tin gói")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String ten = edtTen.getText() != null ? edtTen.getText().toString().trim() : "";
                    if (ten.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập tên công ty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String loaiGoi = rbDungThu.isChecked()
                            ? GoiDangKy.LOAI_GOI_DUNG_THU : GoiDangKy.LOAI_GOI_CHINH_THUC;
                    String trangThai = TRANG_THAI_VALUES[spinnerTs.getSelectedItemPosition()];

                    java.util.Map<String, Object> data = new java.util.HashMap<>();
                    data.put("tenCongTy", ten);
                    data.put("loaiGoi", loaiGoi);
                    data.put("trangThai", trangThai);

                    FirebaseFirestore.getInstance().collection("GoiDangKy").document(docId)
                            .update(data)
                            .addOnSuccessListener(v -> {
                                if (getContext() == null) return;
                                Toast.makeText(getContext(), "Đã cập nhật", Toast.LENGTH_SHORT).show();
                                taiDanhSach();
                            })
                            .addOnFailureListener(e -> {
                                if (getContext() == null) return;
                                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void xacNhanXoa(GoiDangKy goi, String docId) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa gói đăng ký")
                .setMessage("Xóa gói của " + goi.getTenCongTy() + "?\nCác user thuộc công ty này sẽ không thể đánh giá sản phẩm.")
                .setPositiveButton("Xóa", (d, w) -> {
                    FirebaseFirestore.getInstance().collection("GoiDangKy").document(docId)
                            .delete()
                            .addOnSuccessListener(v -> {
                                if (getContext() == null) return;
                                Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
                                taiDanhSach();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}


