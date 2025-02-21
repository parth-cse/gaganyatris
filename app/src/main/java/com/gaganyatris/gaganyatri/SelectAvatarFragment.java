package com.gaganyatris.gaganyatri;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class SelectAvatarFragment extends Fragment {

    public SelectAvatarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_avatar, container, false);

        LinearLayout btnBack = view.findViewById(R.id.btn_back);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                // Use FragmentManager to pop the back stack
                FragmentManager fm = requireActivity().getSupportFragmentManager();
                fm.popBackStack(); // or fm.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE) if you want to pop up to the given tag inclusive
            });
        }

        return view;
    }
}