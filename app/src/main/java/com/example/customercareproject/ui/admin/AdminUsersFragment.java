package com.example.customercareproject.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.NguoiDung;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

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

        String[] vaiTroOptions = {"Tất cả", NguoiDung.VAI_TRO_KHACH_HANG, NguoiDung.VAI_TRO_KTV, NguoiDung.VAI_TRO_ADMIN};
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, vaiTroOptions);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLoc.setAdapter(spAdapter);

        adapter = new UserAdminAdapter(new ArrayList<>());
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUsers.setAdapter(adapter);

        spinnerLoc.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int pos, long id) {
                locDanhSach(edtTimKiem.getText() != null ? edtTimKiem.getText().toString() : "",
                        vaiTroOptions[pos]);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        edtTimKiem.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String vaiTro = vaiTroOptions[spinnerLoc.getSelectedItemPosition()];
                locDanhSach(s.toString(), vaiTro);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

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
            boolean matchVaiTro = "Tất cả".equals(vaiTro) || vaiTro.equals(nd.getVaiTro());
            boolean matchQuery = query.isEmpty()
                    || (nd.getHoTen() != null && nd.getHoTen().toLowerCase().contains(query.toLowerCase()))
                    || (nd.getEmail() != null && nd.getEmail().toLowerCase().contains(query.toLowerCase()));
            if (matchVaiTro && matchQuery) filtered.add(nd);
        }
        adapter.capNhat(filtered);
    }
}
