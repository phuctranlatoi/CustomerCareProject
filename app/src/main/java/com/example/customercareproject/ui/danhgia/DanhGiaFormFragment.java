package com.example.customercareproject.ui.danhgia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.customercareproject.R;
import com.example.customercareproject.model.DanhGia;
import com.example.customercareproject.utils.NlpHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DanhGiaFormFragment extends Fragment {

    private static final String ARG_SAN_PHAM = "sanPham";
    private static final String ARG_LOAI = "loai";

    private String sanPham, loai;
    private int soSaoChon = 0;
    private ImageButton[] stars;
    private TextView tvMucDo;
    private Spinner spinnerBuoc;
    private TextInputEditText edtNoiDung;

    public static DanhGiaFormFragment newInstance(String sanPham, String loai) {
        DanhGiaFormFragment f = new DanhGiaFormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SAN_PHAM, sanPham);
        args.putString(ARG_LOAI, loai);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sanPham = getArguments().getString(ARG_SAN_PHAM);
            loai = getArguments().getString(ARG_LOAI);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_danh_gia_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerBuoc = view.findViewById(R.id.spinnerBuoc);
        tvMucDo = view.findViewById(R.id.tvMucDoHaiLong);
        edtNoiDung = view.findViewById(R.id.edtNoiDung);

        stars = new ImageButton[]{
            view.findViewById(R.id.star1), view.findViewById(R.id.star2),
            view.findViewById(R.id.star3), view.findViewById(R.id.star4),
            view.findViewById(R.id.star5)
        };

        setupSpinner();
        setupStars();

        view.findViewById(R.id.btnGuiDanhGia).setOnClickListener(v -> guiDanhGia());
    }

    private void setupSpinner() {
        String[] buocGiaoDien = {
            "Màn hình đăng nhập", "Màn hình chính", "Menu điều hướng",
            "Màu sắc & Font chữ", "Bố cục tổng thể", "Biểu tượng & Nút bấm", "Khác"
        };
        String[] buocChucNang = {
            "Đăng nhập / Đăng ký", "Tạo mới dữ liệu", "Tìm kiếm & Lọc",
            "Xuất / In báo cáo", "Đồng bộ dữ liệu", "Thông báo", "Khác"
        };

        String[] buoc = "GiaoDien".equals(loai) ? buocGiaoDien : buocChucNang;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, buoc);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBuoc.setAdapter(adapter);
    }

    private void setupStars() {
        String[] labels = {"Rất không hài lòng", "Không hài lòng", "Bình thường", "Hài lòng", "Rất hài lòng"};
        for (int i = 0; i < stars.length; i++) {
            final int idx = i + 1;
            stars[i].setOnClickListener(v -> {
                soSaoChon = idx;
                updateStarUI(idx);
                tvMucDo.setText(labels[idx - 1]);
            });
        }
    }

    private void updateStarUI(int selected) {
        for (int i = 0; i < stars.length; i++) {
            stars[i].setImageResource(i < selected
                    ? android.R.drawable.btn_star_big_on
                    : android.R.drawable.btn_star_big_off);
        }
    }

    private void guiDanhGia() {
        if (soSaoChon == 0) {
            Toast.makeText(getContext(), "Vui lòng chọn mức độ hài lòng", Toast.LENGTH_SHORT).show();
            return;
        }
        String buocChon = spinnerBuoc.getSelectedItem().toString();
        String noiDung = edtNoiDung.getText() != null ? edtNoiDung.getText().toString().trim() : "";

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("NguoiDung")
                .document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String hoTen = doc.getString("hoTen");
                    DanhGia danhGia = new DanhGia(user.getUid(), hoTen != null ? hoTen : "",
                            sanPham, loai, buocChon, soSaoChon, noiDung);
                    // NLP: tự động gắn tag và phân tích cảm xúc
                    danhGia.setTags(NlpHelper.phanTichTag(noiDung.isEmpty() ? buocChon : noiDung));
                    danhGia.setUuTien(NlpHelper.phanTichUuTien(noiDung));
                    danhGia.setCamXuc(NlpHelper.phanTichCamXuc(soSaoChon));

                    FirebaseFirestore.getInstance().collection("DanhGia")
                            .add(danhGia)
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(getContext(), "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                                resetForm();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    private void resetForm() {
        soSaoChon = 0;
        updateStarUI(0);
        tvMucDo.setText("Chưa đánh giá");
        edtNoiDung.setText("");
        spinnerBuoc.setSelection(0);
    }
}
