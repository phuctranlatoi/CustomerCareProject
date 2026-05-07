package com.example.customercareproject.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.customercareproject.R;
import com.example.customercareproject.model.LoiPhatSinh;
import com.example.customercareproject.model.SanPham;
import com.example.customercareproject.model.TemplateTrLoi;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.customercareproject.ui.components.Material3TextField;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminKnowledgeFragment extends Fragment {

    private Spinner spinnerSanPham;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private MaterialButton fabThemLoi;
    private KnowledgePagerAdapter pagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_knowledge, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerSanPham = view.findViewById(R.id.spinnerSanPham);
        viewPager = view.findViewById(R.id.viewPagerKnowledge);
        tabLayout = view.findViewById(R.id.tabLayoutKnowledge);
        fabThemLoi = view.findViewById(R.id.fabThemLoi);

        // Setup spinner
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, SanPham.DANH_SACH);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSanPham.setAdapter(spAdapter);

        // Setup ViewPager2 với sanPham ban đầu
        String sanPhamBanDau = SanPham.DANH_SACH[0];
        setupViewPager(sanPhamBanDau);

        // Khi spinner thay đổi → recreate adapter với sanPham mới
        spinnerSanPham.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int pos, long id) {
                String sanPham = SanPham.DANH_SACH[pos];
                setupViewPager(sanPham);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // FAB: hiện/ẩn theo tab active
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // Tab 0 = Lỗi phát sinh → hiện FAB
                // Tab 1 = Template → ẩn FAB (tab Template có nút riêng)
                fabThemLoi.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
            }
        });

        // FAB click → thêm lỗi (chỉ active khi tab 0)
        fabThemLoi.setOnClickListener(v -> {
            String sanPham = SanPham.DANH_SACH[spinnerSanPham.getSelectedItemPosition()];
            hienDialogThemLoi(sanPham);
        });
    }

    private void setupViewPager(String sanPham) {
        pagerAdapter = new KnowledgePagerAdapter(getChildFragmentManager(), getLifecycle(), sanPham);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Lỗi phát sinh" : "Template");
        }).attach();

        // Đảm bảo FAB visibility đúng với tab hiện tại
        fabThemLoi.setVisibility(viewPager.getCurrentItem() == 0 ? View.VISIBLE : View.GONE);
    }

    private void hienDialogThemLoi(String sanPham) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_them_loi, null);
        com.example.customercareproject.ui.components.Material3TextField edtTieuDe = dialogView.findViewById(R.id.edtTieuDe);
        com.example.customercareproject.ui.components.Material3TextField edtMoTa = dialogView.findViewById(R.id.edtMoTa);
        com.example.customercareproject.ui.components.Material3TextField edtCachGiaiQuyet = dialogView.findViewById(R.id.edtCachGiaiQuyet);

        new AlertDialog.Builder(getContext())
                .setTitle("Thêm lỗi vào Knowledge Base")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String tieuDe = edtTieuDe.getText() != null ? edtTieuDe.getText().toString().trim() : "";
                    String moTa = edtMoTa.getText() != null ? edtMoTa.getText().toString().trim() : "";
                    String cachGQ = edtCachGiaiQuyet.getText() != null ? edtCachGiaiQuyet.getText().toString().trim() : "";

                    if (tieuDe.isEmpty() || moTa.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập tiêu đề và mô tả", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("sanPham", sanPham);
                    data.put("tieuDe", tieuDe);
                    data.put("moTa", moTa);
                    data.put("cachGiaiQuyet", cachGQ);
                    data.put("coHuongDan", !cachGQ.isEmpty());
                    data.put("taoLuc", Timestamp.now());

                    FirebaseFirestore.getInstance().collection("LoiPhatSinh").add(data)
                            .addOnSuccessListener(ref ->
                                    Toast.makeText(getContext(), "Đã thêm!", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ── ViewPager2 Adapter ────────────────────────────────────────────────

    private static class KnowledgePagerAdapter extends FragmentStateAdapter {

        private final String sanPham;

        KnowledgePagerAdapter(@NonNull FragmentManager fm, @NonNull Lifecycle lifecycle, String sanPham) {
            super(fm, lifecycle);
            this.sanPham = sanPham;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return LoiPhatSinhTabFragment.newInstance(sanPham);
            } else {
                return TemplateTabFragment.newInstance(sanPham);
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    // ── Tab 0: Lỗi Phát Sinh ─────────────────────────────────────────────

    public static class LoiPhatSinhTabFragment extends Fragment {

        private static final String ARG_SAN_PHAM = "sanPham";

        private KnowledgeAdminAdapter adapter;
        private FirebaseFirestore db;

        public static LoiPhatSinhTabFragment newInstance(String sanPham) {
            LoiPhatSinhTabFragment f = new LoiPhatSinhTabFragment();
            Bundle args = new Bundle();
            args.putString(ARG_SAN_PHAM, sanPham);
            f.setArguments(args);
            return f;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            // Dùng layout đơn giản chỉ có RecyclerView
            RecyclerView rv = new RecyclerView(requireContext());
            rv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            return rv;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            db = FirebaseFirestore.getInstance();

            RecyclerView rv = (RecyclerView) view;
            adapter = new KnowledgeAdminAdapter(new ArrayList<>(), this::xoaLoi);
            rv.setAdapter(adapter);

            String sanPham = getArguments() != null ? getArguments().getString(ARG_SAN_PHAM, "") : "";
            taiDanhSachLoi(sanPham);
        }

        private void taiDanhSachLoi(String sanPham) {
            db.collection("LoiPhatSinh").whereEqualTo("sanPham", sanPham)
                    .addSnapshotListener((snap, e) -> {
                        if (snap == null || getContext() == null) return;
                        List<LoiPhatSinh> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snap) {
                            LoiPhatSinh loi = doc.toObject(LoiPhatSinh.class);
                            loi.setId(doc.getId());
                            list.add(loi);
                        }
                        adapter.capNhat(list);
                    });
        }

        private void xoaLoi(String loiId) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa lỗi này?")
                    .setPositiveButton("Xóa", (d, w) ->
                            db.collection("LoiPhatSinh").document(loiId).delete()
                                    .addOnSuccessListener(v ->
                                            Toast.makeText(getContext(), "Đã xóa!", Toast.LENGTH_SHORT).show()))
                    .setNegativeButton("Hủy", null)
                    .show();
        }
    }

    // ── Tab 1: Template ───────────────────────────────────────────────────

    public static class TemplateTabFragment extends Fragment {

        private static final String ARG_SAN_PHAM = "sanPham";

        private TemplateAdminAdapter adapter;
        private FirebaseFirestore db;
        private TextView tvEmpty;

        public static TemplateTabFragment newInstance(String sanPham) {
            TemplateTabFragment f = new TemplateTabFragment();
            Bundle args = new Bundle();
            args.putString(ARG_SAN_PHAM, sanPham);
            f.setArguments(args);
            return f;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_template_list, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            db = FirebaseFirestore.getInstance();

            RecyclerView rvTemplate = view.findViewById(R.id.rvTemplateAdmin);
            MaterialButton btnThem = view.findViewById(R.id.btnThemTemplate);
            tvEmpty = view.findViewById(R.id.tvEmptyTemplateAdmin);

            adapter = new TemplateAdminAdapter(new ArrayList<>(), this::xoaTemplate);
            rvTemplate.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTemplate.setAdapter(adapter);

            String sanPham = getArguments() != null ? getArguments().getString(ARG_SAN_PHAM, "") : "";
            taiDanhSachTemplate(sanPham);

            btnThem.setOnClickListener(v -> hienDialogThemTemplate(sanPham));
        }

        private void taiDanhSachTemplate(String sanPham) {
            db.collection("TemplateTrLoi").whereEqualTo("sanPham", sanPham)
                    .addSnapshotListener((snap, e) -> {
                        if (snap == null || getContext() == null) return;
                        List<TemplateTrLoi> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snap) {
                            TemplateTrLoi t = doc.toObject(TemplateTrLoi.class);
                            t.setId(doc.getId());
                            list.add(t);
                        }
                        adapter.capNhat(list);
                        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    });
        }

        private void hienDialogThemTemplate(String sanPham) {
            View dialogView = LayoutInflater.from(getContext())
                    .inflate(R.layout.dialog_them_template, null);
            com.example.customercareproject.ui.components.Material3TextField edtTieuDe = dialogView.findViewById(R.id.edtTieuDeTemplate);
            com.example.customercareproject.ui.components.Material3TextField edtNoiDung = dialogView.findViewById(R.id.edtNoiDungTemplate);

            new AlertDialog.Builder(getContext())
                    .setTitle("Thêm template")
                    .setView(dialogView)
                    .setPositiveButton("Lưu", (dialog, which) -> {
                        String tieuDe = edtTieuDe.getText() != null ? edtTieuDe.getText().toString().trim() : "";
                        String noiDung = edtNoiDung.getText() != null ? edtNoiDung.getText().toString().trim() : "";

                        if (tieuDe.isEmpty() || noiDung.isEmpty()) {
                            Toast.makeText(getContext(),
                                    "Vui lòng nhập tiêu đề và nội dung template",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Map<String, Object> data = new HashMap<>();
                        data.put("tieuDe", tieuDe);
                        data.put("noiDung", noiDung);
                        data.put("sanPham", sanPham);
                        data.put("taoLuc", Timestamp.now());

                        db.collection("TemplateTrLoi").add(data)
                                .addOnSuccessListener(ref ->
                                        Toast.makeText(getContext(), "Đã thêm template!", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }

        private void xoaTemplate(TemplateTrLoi template) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa template \"" + template.getTieuDe() + "\"?")
                    .setPositiveButton("Xóa", (d, w) ->
                            db.collection("TemplateTrLoi").document(template.getId()).delete()
                                    .addOnSuccessListener(v ->
                                            Toast.makeText(getContext(), "Đã xóa template!", Toast.LENGTH_SHORT).show()))
                    .setNegativeButton("Hủy", null)
                    .show();
        }
    }
}


