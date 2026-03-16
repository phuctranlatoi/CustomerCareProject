package com.example.customercareproject.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.YeuCauHoTro;
import com.example.customercareproject.ui.ktv.TicketAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminTicketsFragment extends Fragment {

    private TicketAdapter adapter;
    private ListenerRegistration listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        RecyclerView rv = new RecyclerView(requireContext());
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setPadding(8, 8, 8, 8);
        rv.setClipToPadding(false);
        adapter = new TicketAdapter(new ArrayList<>(), ticket -> {});
        rv.setAdapter(adapter);
        return rv;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
                    adapter.capNhat(list);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listener != null) listener.remove();
    }
}
