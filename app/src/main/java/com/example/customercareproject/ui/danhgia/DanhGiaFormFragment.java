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
import android.widget.LinearLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.customercareproject.R;
import com.example.customercareproject.model.DanhGia;
import com.example.customercareproject.model.GoiDangKy;
import com.example.customercareproject.ui.components.Material3TextField;
import com.example.customercareproject.ui.components.Material3Button;
import com.example.customercareproject.utils.AnimationHelper;
import com.example.customercareproject.utils.NlpHelper;
import com.example.customercareproject.utils.BadRatingHandler;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.ArrayList;

public class DanhGiaFormFragment extends Fragment {

    private static final String ARG_SAN_PHAM = "sanPham";
    private static final String ARG_LOAI     = "loai";
    private static final String ARG_LOAI_GOI = "loaiGoi";

    private String sanPham, loai, loaiGoiArg;

    // Thông tin gói đăng ký (lấy từ Firestore)
    private String maSoThueUser;
    private String tenCongTyUser;
    private String loaiGoiThucTe; // loaiGoi thực tế từ GoiDangKy

    private int soSaoChon = 0;
    private ImageButton[] stars;
    private TextView tvMucDo;
    private Spinner spinnerBuoc;
    private Material3TextField txtNoiDung;
    private View formContainer;
    private MaterialCardView cardThongBaoQuyen;
    private TextView tvThongBaoQuyen;
    private MaterialButton btnGuiDanhGia;
    private LinearLayout layoutSuccess;
    private ImageView imgSuccess;
    
    // Issue cards
    private MaterialCardView[] issueCards;
    private List<String> selectedIssues = new ArrayList<>();

    public static DanhGiaFormFragment newInstance(String sanPham, String loai, String loaiGoi) {
        DanhGiaFormFragment f = new DanhGiaFormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SAN_PHAM, sanPham);
        args.putString(ARG_LOAI, loai);
        args.putString(ARG_LOAI_GOI, loaiGoi);
        f.setArguments(args);
        return f;
    }

    // Backward compat
    public static DanhGiaFormFragment newInstance(String sanPham, String loai) {
        return newInstance(sanPham, loai, null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sanPham = getArguments().getString(ARG_SAN_PHAM);
            loai = getArguments().getString(ARG_LOAI);
            loaiGoiArg = getArguments().getString(ARG_LOAI_GOI);
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

        initViews(view);
        setupSpinner();
        setupStars();
        setupIssueCards();
        setupSubmitButton();

        // Kiểm tra quyền đánh giá theo gói
        kiemTraQuyenDanhGia();
    }

    private void initViews(View view) {
        formContainer = view.findViewById(R.id.formContainer);
        cardThongBaoQuyen = view.findViewById(R.id.cardThongBaoQuyen);
        tvThongBaoQuyen = view.findViewById(R.id.tvThongBaoQuyen);
        spinnerBuoc = view.findViewById(R.id.spinnerBuoc);
        tvMucDo = view.findViewById(R.id.tvMucDoHaiLong);
        txtNoiDung = view.findViewById(R.id.txtNoiDung);
        btnGuiDanhGia = view.findViewById(R.id.btnGuiDanhGia);
        layoutSuccess = view.findViewById(R.id.layoutSuccess);
        imgSuccess = view.findViewById(R.id.imgSuccess);

        stars = new ImageButton[]{
            view.findViewById(R.id.star1), view.findViewById(R.id.star2),
            view.findViewById(R.id.star3), view.findViewById(R.id.star4),
            view.findViewById(R.id.star5)
        };

        issueCards = new MaterialCardView[]{
            view.findViewById(R.id.cardIssue1), view.findViewById(R.id.cardIssue2),
            view.findViewById(R.id.cardIssue3), view.findViewById(R.id.cardIssue4)
        };
    }

    /** Truy vấn GoiDangKy, kiểm tra sanPham có trong gói không */
    private void kiemTraQuyenDanhGia() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            hienThiLoi("Vui lòng đăng nhập để đánh giá.");
            return;
        }

        FirebaseFirestore.getInstance().collection("NguoiDung")
                .document(user.getUid()).get()
                .addOnSuccessListener(nguoiDungDoc -> {
                    maSoThueUser = nguoiDungDoc.getString("maSoThue");
                    tenCongTyUser = nguoiDungDoc.getString("tenCongTy");

                    if (maSoThueUser == null || maSoThueUser.isEmpty()) {
                        // Không có MST → cho phép đánh giá (fallback)
                        hienThiForm();
                        return;
                    }

                    FirebaseFirestore.getInstance().collection("GoiDangKy")
                            .whereEqualTo("maSoThue", maSoThueUser)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(snap -> {
                                if (snap.isEmpty()) {
                                    hienThiLoi("Doanh nghiệp của bạn chưa có gói đăng ký đang hoạt động.");
                                    return;
                                }

                                GoiDangKy goi = snap.getDocuments().get(0).toObject(GoiDangKy.class);
                                if (goi == null || !GoiDangKy.TRANG_THAI_HOAT_DONG.equals(goi.getTrangThai())) {
                                    hienThiLoi("Gói dịch vụ của doanh nghiệp đã hết hạn hoặc tạm dừng.");
                                    return;
                                }

                                List<String> danhSachSp = goi.getSanPhamDangKy();
                                if (danhSachSp == null || !danhSachSp.contains(sanPham)) {
                                    hienThiLoi("Bạn không có quyền đánh giá sản phẩm này.\nSản phẩm chưa được đăng ký trong gói dịch vụ.");
                                    return;
                                }

                                // Lấy loaiGoi theo từng sản phẩm cụ thể
                                loaiGoiThucTe = goi.getLoaiCuaSanPham(sanPham);
                                hienThiForm();
                            })
                            .addOnFailureListener(e -> hienThiForm()); // fallback nếu lỗi mạng
                })
                .addOnFailureListener(e -> hienThiForm());
    }

    private void hienThiForm() {
        if (getView() == null) return;
        if (formContainer != null) {
            formContainer.setVisibility(View.VISIBLE);
            // Apply fade-in animation when form is shown
            AnimationHelper.fadeIn(formContainer);
        }
        if (cardThongBaoQuyen != null) cardThongBaoQuyen.setVisibility(View.GONE);
    }

    private void hienThiLoi(String thongBao) {
        if (getView() == null) return;
        if (formContainer != null) formContainer.setVisibility(View.GONE);
        if (cardThongBaoQuyen != null && tvThongBaoQuyen != null) {
            cardThongBaoQuyen.setVisibility(View.VISIBLE);
            tvThongBaoQuyen.setText(thongBao);
            // Apply fade-in animation for error message
            AnimationHelper.fadeIn(cardThongBaoQuyen);
        }
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
                // Apply scale animation when star is tapped (Requirement 13.2)
                AnimationHelper.scalePress(v);
                
                // Update rating after animation
                v.postDelayed(() -> {
                    AnimationHelper.scaleRelease(v);
                    soSaoChon = idx;
                    updateStarUI(idx);
                    tvMucDo.setText(labels[idx - 1]);
                }, AnimationHelper.DURATION_FAST);
            });
        }
    }

    private void updateStarUI(int selected) {
        for (int i = 0; i < stars.length; i++) {
            stars[i].setImageResource(i < selected
                    ? R.drawable.ic_star_filled
                    : R.drawable.ic_star_outline);
        }
    }

    private void setupIssueCards() {
        String[] issueLabels = {"Chậm", "Lỗi", "Khó hiểu", "Thiếu tính năng"};
        
        for (int i = 0; i < issueCards.length; i++) {
            final int index = i;
            final String issueLabel = issueLabels[i];
            
            issueCards[i].setOnClickListener(v -> {
                // Toggle card selection
                boolean isSelected = issueCards[index].isChecked();
                issueCards[index].setChecked(!isSelected);
                
                // Apply scale animation
                AnimationHelper.scalePress(v);
                v.postDelayed(() -> AnimationHelper.scaleRelease(v), AnimationHelper.DURATION_FAST);
                
                // Update selected issues list
                if (!isSelected) {
                    selectedIssues.add(issueLabel);
                } else {
                    selectedIssues.remove(issueLabel);
                }
            });
        }
    }

    private void setupSubmitButton() {
        btnGuiDanhGia.setOnClickListener(v -> {
            // Apply button press animation
            AnimationHelper.scalePress(v);
            v.postDelayed(() -> {
                AnimationHelper.scaleRelease(v);
                guiDanhGia();
            }, AnimationHelper.DURATION_FAST);
        });
    }

    private void guiDanhGia() {
        if (soSaoChon == 0) {
            Toast.makeText(getContext(), "Vui lòng chọn mức độ hài lòng", Toast.LENGTH_SHORT).show();
            // Apply shake animation to star rating section
            AnimationHelper.shake(findViewById(R.id.layoutSao));
            return;
        }
        String buocChon = spinnerBuoc.getSelectedItem().toString();
        String noiDung = txtNoiDung.getText().trim();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Disable button and show loading state
        btnGuiDanhGia.setEnabled(false);
        btnGuiDanhGia.setText("Đang gửi...");
        Toast.makeText(getContext(), "Đang phân tích...", Toast.LENGTH_SHORT).show();

        final int soSaoFinal = soSaoChon;
        final String sanPhamFinal = sanPham;
        final String loaiGoiFinal = loaiGoiThucTe;
        final String maSoThueFinal = maSoThueUser;
        final String tenCongTyFinal = tenCongTyUser;

        FirebaseFirestore.getInstance().collection("NguoiDung")
                .document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String hoTen = doc.getString("hoTen");
                    DanhGia danhGia = new DanhGia(user.getUid(), hoTen != null ? hoTen : "",
                            sanPhamFinal, loai, buocChon, soSaoFinal, noiDung);

                    // Gắn thông tin gói và công ty
                    danhGia.setLoaiGoi(loaiGoiFinal);
                    danhGia.setMaSoThue(maSoThueFinal);
                    danhGia.setTenCongTy(tenCongTyFinal);
                    
                    // Add selected issues to the rating
                    if (!selectedIssues.isEmpty()) {
                        danhGia.setTags(selectedIssues);
                    }

                    NlpHelper.phanTichGemini(
                            noiDung.isEmpty() ? buocChon : noiDung,
                            soSaoFinal,
                            sanPhamFinal,
                            new NlpHelper.GeminiCallback() {
                                @Override
                                public void onResult(java.util.List<String> tags, String camXuc,
                                                     String uuTien, String tomTat) {
                                    // Merge NLP tags with selected issues
                                    List<String> allTags = new ArrayList<>(selectedIssues);
                                    if (tags != null) {
                                        for (String tag : tags) {
                                            if (!allTags.contains(tag)) {
                                                allTags.add(tag);
                                            }
                                        }
                                    }
                                    
                                    danhGia.setTags(allTags);
                                    danhGia.setCamXuc(camXuc);
                                    danhGia.setUuTien(uuTien);
                                    if (!tomTat.isEmpty()) danhGia.setNoiDung(
                                            noiDung.isEmpty() ? tomTat : noiDung);

                                    FirebaseFirestore.getInstance().collection("DanhGia")
                                            .add(danhGia)
                                            .addOnSuccessListener(ref -> {
                                                if (getActivity() == null) return;
                                                getActivity().runOnUiThread(() -> {
                                                    // Xử lý đánh giá sau khi lưu thành công
                                                    BadRatingHandler.handleRatingSubmitted(
                                                        getContext(), danhGia, ref.getId());
                                                    
                                                    showSuccessAnimation();
                                                });
                                            })
                                            .addOnFailureListener(e -> {
                                                if (getActivity() == null) return;
                                                getActivity().runOnUiThread(() -> {
                                                    Toast.makeText(getContext(),
                                                            "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    resetButtonState();
                                                });
                                            });
                                }

                                @Override
                                public void onError(String error) {
                                    if (getActivity() == null) return;
                                    getActivity().runOnUiThread(() -> {
                                        // NLP lỗi → vẫn lưu đánh giá với giá trị mặc định
                                        // để đảm bảo không mất dữ liệu (đặc biệt đánh giá xấu)
                                        android.util.Log.w("DanhGia", "NLP lỗi, lưu fallback: " + error);
                                        
                                        // Gán giá trị fallback dựa trên số sao
                                        if (soSaoFinal <= 2) {
                                            danhGia.setCamXuc("KhongHaiLong");
                                            danhGia.setUuTien("Cao");
                                        } else if (soSaoFinal == 3) {
                                            danhGia.setCamXuc("TrungBinh");
                                            danhGia.setUuTien("TrungBinh");
                                        } else {
                                            danhGia.setCamXuc("HaiLong");
                                            danhGia.setUuTien("Thap");
                                        }
                                        
                                        // Giữ tags từ selectedIssues (user đã chọn thủ công)
                                        if (!selectedIssues.isEmpty()) {
                                            danhGia.setTags(new ArrayList<>(selectedIssues));
                                        }
                                        
                                        // Lưu vào Firestore dù NLP lỗi
                                        FirebaseFirestore.getInstance().collection("DanhGia")
                                            .add(danhGia)
                                            .addOnSuccessListener(ref -> {
                                                if (getActivity() == null) return;
                                                getActivity().runOnUiThread(() -> {
                                                    // Vẫn xử lý đánh giá xấu
                                                    BadRatingHandler.handleRatingSubmitted(
                                                        getContext(), danhGia, ref.getId());
                                                    showSuccessAnimation();
                                                });
                                            })
                                            .addOnFailureListener(e -> {
                                                if (getActivity() == null) return;
                                                getActivity().runOnUiThread(() -> {
                                                    Toast.makeText(getContext(),
                                                        "Lỗi lưu đánh giá: " + e.getMessage(), 
                                                        Toast.LENGTH_SHORT).show();
                                                    resetButtonState();
                                                });
                                            });
                                    });
                                }
                            }
                    );
                });
    }

    private View findViewById(int id) {
        return getView() != null ? getView().findViewById(id) : null;
    }

    private void showSuccessAnimation() {
        // Hide form and show success animation (Requirement 13.5)
        AnimationHelper.fadeOut(formContainer);
        
        // Show success layout with animation
        layoutSuccess.setVisibility(View.VISIBLE);
        AnimationHelper.fadeIn(layoutSuccess);
        
        // Apply bounce animation to success checkmark
        AnimationHelper.bounce(imgSuccess);
        
        // Auto-hide success message and reset form after 3 seconds
        layoutSuccess.postDelayed(() -> {
            AnimationHelper.fadeOut(layoutSuccess);
            layoutSuccess.postDelayed(() -> {
                resetForm();
                AnimationHelper.fadeIn(formContainer);
            }, AnimationHelper.DURATION_MEDIUM);
        }, 3000);
    }

    private void resetButtonState() {
        if (btnGuiDanhGia != null) {
            btnGuiDanhGia.setEnabled(true);
            btnGuiDanhGia.setText("Gửi đánh giá");
        }
    }

    private void resetForm() {
        soSaoChon = 0;
        updateStarUI(0);
        tvMucDo.setText("Chưa đánh giá");
        txtNoiDung.setText("");
        spinnerBuoc.setSelection(0);
        
        // Reset issue cards
        for (MaterialCardView card : issueCards) {
            card.setChecked(false);
        }
        selectedIssues.clear();
        
        // Reset button state
        resetButtonState();
    }
}


