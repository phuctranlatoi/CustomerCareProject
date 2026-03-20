package com.example.customercareproject.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.NguoiDung;
import com.example.customercareproject.model.SanPham;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminUsersFragment extends Fragment {

    private UserAdminAdapter adapter;
    private List<NguoiDung> allUsers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvUsers = view.findViewById(R.id.rvUsers);
        TextInputEditText edtTimKiem = view.findViewById(R.id.edtTimKiemUser);
        Spinner spinnerLoc = view.findViewById(R.id.spinnerLocVaiTro);
        FloatingActionButton fabTaoKtv = view.findViewById(R.id.fabTaoKtv);

        // Options dung de loc (gia tri thuc)
        String[] vaiTroOptions = {"", NguoiDung.VAI_TRO_KHACH_HANG, NguoiDung.VAI_TRO_KTV, NguoiDung.VAI_TRO_ADMIN};
        // Labels hien thi
        String[] vaiTroLabels = {"Tat ca", "Khach hang", "KTV", "Admin"};

        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, vaiTroLabels);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLoc.setAdapter(spAdapter);

        adapter = new UserAdminAdapter(new ArrayList<>());
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUsers.setAdapter(adapter);

        spinnerLoc.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int pos, long id) {
                String query = edtTimKiem.getText() != null ? edtTimKiem.getText().toString() : "";
                locDanhSach(query, vaiTroOptions[pos]);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        edtTimKiem.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                locDanhSach(s.toString(), vaiTroOptions[spinnerLoc.getSelectedItemPosition()]);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        fabTaoKtv.setOnClickListener(v -> hienDialogTaoKtv());
        taiDanhSachUser();
    }

    private void taiDanhSachUser() {
        FirebaseFirestore.getInstance().collection("NguoiDung").get()
                .addOnSuccessListener(snap -> {
                    allUsers.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        NguoiDung nd = doc.toObject(NguoiDung.class);
                        nd.setUid(doc.getId());
                        allUsers.add(nd);
                    }
                    adapter.capNhat(allUsers);
                });
    }

    private void locDanhSach(String query, String vaiTro) {
        List<NguoiDung> filtered = new ArrayList<>();
        for (NguoiDung nd : allUsers) {
            boolean matchVaiTro = vaiTro.isEmpty() || vaiTro.equals(nd.getVaiTro());
            boolean matchQuery = query.isEmpty()
                    || (nd.getHoTen() != null && nd.getHoTen().toLowerCase().contains(query.toLowerCase()))
                    || (nd.getEmail() != null && nd.getEmail().toLowerCase().contains(query.toLowerCase()));
            if (matchVaiTro && matchQuery) filtered.add(nd);
        }
        adapter.capNhat(filtered);
    }

    private void hienDialogTaoKtv() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_tao_ktv, null);
        TextInputEditText edtHoTen = dialogView.findViewById(R.id.edtHoTenKtv);
        TextInputEditText edtEmail = dialogView.findViewById(R.id.edtEmailKtv);
        TextInputEditText edtSdt = dialogView.findViewById(R.id.edtSdtKtv);
        TextInputEditText edtMatKhau = dialogView.findViewById(R.id.edtMatKhauKtv);
        LinearLayout layoutChuyenMon = dialogView.findViewById(R.id.layoutChuyenMon);

        CheckBox[] checkBoxes = new CheckBox[SanPham.DANH_SACH.length];
        for (int i = 0; i < SanPham.DANH_SACH.length; i++) {
            CheckBox cb = new CheckBox(getContext());
            cb.setText(SanPham.DANH_SACH[i]);
            cb.setPadding(0, 4, 0, 4);
            layoutChuyenMon.addView(cb);
            checkBoxes[i] = cb;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Tao tai khoan Ky Thuat Vien")
                .setView(dialogView)
                .setPositiveButton("Tao", (dialog, which) -> {
                    String hoTen = edtHoTen.getText() != null ? edtHoTen.getText().toString().trim() : "";
                    String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
                    String sdt = edtSdt.getText() != null ? edtSdt.getText().toString().trim() : "";
                    String matKhau = edtMatKhau.getText() != null ? edtMatKhau.getText().toString().trim() : "";

                    if (hoTen.isEmpty() || email.isEmpty() || matKhau.length() < 6) {
                        Toast.makeText(getContext(), "Vui long dien day du (mat khau >= 6 ky tu)", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> chuyenMon = new ArrayList<>();
                    for (int i = 0; i < checkBoxes.length; i++) {
                        if (checkBoxes[i].isChecked()) chuyenMon.add(SanPham.DANH_SACH[i]);
                    }
                    if (chuyenMon.isEmpty()) chuyenMon = Arrays.asList(SanPham.DANH_SACH);

                    taoTaiKhoanKtv(hoTen, email, sdt, matKhau, chuyenMon);
                })
                .setNegativeButton("Huy", null)
                .show();
    }

    private void taoTaiKhoanKtv(String hoTen, String email, String sdt,
                                  String matKhau, List<String> chuyenMon) {
        // Dung secondary FirebaseApp de tranh mat session Admin hien tai
        FirebaseApp secondaryApp;
        try {
            secondaryApp = FirebaseApp.getInstance("secondary");
        } catch (IllegalStateException e) {
            FirebaseOptions options = FirebaseApp.getInstance().getOptions();
            secondaryApp = FirebaseApp.initializeApp(requireContext(), options, "secondary");
        }

        FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);
        final FirebaseApp appRef = secondaryApp;

        secondaryAuth.createUserWithEmailAndPassword(email, matKhau)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    // Dang xuat khoi secondary app ngay lap tuc
                    secondaryAuth.signOut();

                    Map<String, Object> data = new HashMap<>();
                    data.put("uid", uid);
                    data.put("hoTen", hoTen);
                    data.put("email", email);
                    data.put("soDienThoai", sdt);
                    data.put("vaiTro", NguoiDung.VAI_TRO_KTV);
                    data.put("trangThai", NguoiDung.TRANG_THAI_OFFLINE);
                    data.put("soTicketDangXuLy", 0);
                    data.put("tongTicketDaXuLy", 0);
                    data.put("chuyenMon", chuyenMon);
                    data.put("taoLuc", Timestamp.now());

                    FirebaseFirestore.getInstance().collection("NguoiDung")
                            .document(uid)
                            .set(data)
                            .addOnSuccessListener(v -> {
                                Toast.makeText(getContext(),
                                        "Da tao KTV: " + hoTen, Toast.LENGTH_SHORT).show();
                                taiDanhSachUser();
                            })
                            .addOnFailureListener(e2 -> {
                                Toast.makeText(getContext(),
                                        "Loi luu thong tin: " + e2.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Loi tao tai khoan: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
