package com.example.customercareproject.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.SanPham;
import com.example.customercareproject.model.YeuCauHoTro;
import com.example.customercareproject.ui.ktv.TicketAdapter;
import com.example.customercareproject.utils.SmartRouter;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class AdminTicketsFragment extends Fragment {

    private TicketAdapter adapter;
    private ListenerRegistration listener;
    private SwitchMaterial switchQuaHan;
    private Spinner spinnerSanPham, spinnerKtv;
    private String filterSanPham = "";
    private String filterKtvTen = "";
    private List<String> danhSachKtvTen = new ArrayList<>();
    private View emptyState;
    private RecyclerView rv;

    private List<YeuCauHoTro> danhSachGoc = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_tickets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = view.findViewById(R.id.rvTickets);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyState = view.findViewById(R.id.emptyState);
        adapter = new TicketAdapter(new ArrayList<>(), ticket -> {
            if ("HangCho".equals(ticket.getTrangThai())) {
                hienDialogAssignKtv(ticket);
            } else {
                Intent intent = new Intent(getContext(), com.example.customercareproject.ui.ktv.KtvTicketDetailActivity.class);
                intent.putExtra("ticketId", ticket.getId());
                intent.putExtra("readOnly", true);
                startActivity(intent);
            }
        });
        rv.setAdapter(adapter);

        switchQuaHan = view.findViewById(R.id.switchQuaHan);
        switchQuaHan.setOnCheckedChangeListener((buttonView, isChecked) -> apDungFilter());

        // Spinner sản phẩm
        spinnerSanPham = view.findViewById(R.id.spinnerSanPham);
        spinnerKtv = view.findViewById(R.id.spinnerKtv);

        List<String> spList = new ArrayList<>();
        spList.add("Tất cả SP");
        spList.addAll(Arrays.asList(SanPham.DANH_SACH));
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spList);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSanPham.setAdapter(spAdapter);
        spinnerSanPham.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View v, int pos, long id) {
                filterSanPham = pos == 0 ? "" : spList.get(pos);
                apDungFilter();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Spinner KTV — load từ Firestore
        FirebaseFirestore.getInstance().collection("NguoiDung")
                .whereEqualTo("vaiTro", "KTV")
                .get()
                .addOnSuccessListener(snap -> {
                    if (getContext() == null) return;
                    danhSachKtvTen.clear();
                    danhSachKtvTen.add("Tất cả KTV");
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snap) {
                        String ten = doc.getString("hoTen");
                        if (ten != null) danhSachKtvTen.add(ten);
                    }
                    ArrayAdapter<String> ktvAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, danhSachKtvTen);
                    ktvAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerKtv.setAdapter(ktvAdapter);
                    spinnerKtv.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View v, int pos, long id) {
                            filterKtvTen = pos == 0 ? "" : danhSachKtvTen.get(pos);
                            apDungFilter();
                        }
                        @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                    });
                });

        listener = FirebaseFirestore.getInstance().collection("YeuCauHoTro")
                .orderBy("taoLuc", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;
                    List<YeuCauHoTro> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        YeuCauHoTro t = doc.toObject(YeuCauHoTro.class);
                        t.setId(doc.getId());
                        list.add(t);
                    }
                    // Sort: ticket quá hạn lên đầu
                    list.sort(Comparator.comparingInt(t -> isQuaHan(t) ? 0 : 1));
                    danhSachGoc = list;
                    apDungFilter();
                });
    }

    /**
     * Kiểm tra ticket có quá hạn không:
     * taoLuc != null VÀ trangThai là "HangCho"/"ChoXuLy" VÀ elapsed > 30 phút
     */
    public boolean isQuaHan(YeuCauHoTro t) {
        if (t.getTaoLuc() == null) return false;
        String trangThai = t.getTrangThai();
        if (!"HangCho".equals(trangThai) && !"ChoXuLy".equals(trangThai)) return false;
        long elapsed = (System.currentTimeMillis() - t.getTaoLuc().toDate().getTime()) / 60000L;
        return elapsed > 30;
    }

    /**
     * Áp dụng filter theo trạng thái switch, sản phẩm và KTV, rồi cập nhật adapter
     */
    private void apDungFilter() {
        if (switchQuaHan == null) return;
        List<YeuCauHoTro> filtered = new ArrayList<>(danhSachGoc);
        if (switchQuaHan.isChecked()) {
            List<YeuCauHoTro> quaHan = new ArrayList<>();
            for (YeuCauHoTro t : filtered) { if (isQuaHan(t)) quaHan.add(t); }
            filtered = quaHan;
        }
        if (!filterSanPham.isEmpty()) {
            List<YeuCauHoTro> bySp = new ArrayList<>();
            for (YeuCauHoTro t : filtered) { if (filterSanPham.equals(t.getSanPham())) bySp.add(t); }
            filtered = bySp;
        }
        if (!filterKtvTen.isEmpty()) {
            List<YeuCauHoTro> byKtv = new ArrayList<>();
            for (YeuCauHoTro t : filtered) { if (filterKtvTen.equals(t.getKtvTen())) byKtv.add(t); }
            filtered = byKtv;
        }
        adapter.capNhat(filtered);
        if (emptyState != null) emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        if (rv != null) rv.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void hienDialogAssignKtv(YeuCauHoTro ticket) {
        if (getContext() == null) return;
        FirebaseFirestore.getInstance().collection("NguoiDung")
            .whereEqualTo("vaiTro", "KTV")
            .whereEqualTo("trangThai", "Ran")
            .get()
            .addOnSuccessListener(snap -> {
                if (getContext() == null) return;
                List<String> ktvNames = new ArrayList<>();
                List<String> ktvUids = new ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snap) {
                    String ten = doc.getString("hoTen");
                    if (ten != null) {
                        ktvNames.add(ten);
                        ktvUids.add(doc.getId());
                    }
                }
                if (ktvNames.isEmpty()) {
                    Toast.makeText(getContext(), "Không có KTV nào đang rảnh", Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] items = ktvNames.toArray(new String[0]);
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Chọn KTV để assign")
                    .setItems(items, (dialog, which) -> {
                        String ktvUid = ktvUids.get(which);
                        String ktvTen = ktvNames.get(which);
                        FirebaseFirestore.getInstance().collection("YeuCauHoTro")
                            .document(ticket.getId())
                            .update("ktvUid", ktvUid,
                                    "ktvTen", ktvTen,
                                    "trangThai", "ChoXuLy",
                                    "capNhatLuc", com.google.firebase.firestore.FieldValue.serverTimestamp())
                            .addOnSuccessListener(v -> {
                                SmartRouter.tangTicketKtv(ktvUid);
                                Toast.makeText(getContext(), "Đã assign cho " + ktvTen, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(ex ->
                                Toast.makeText(getContext(), "Lỗi: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listener != null) listener.remove();
    }
}
