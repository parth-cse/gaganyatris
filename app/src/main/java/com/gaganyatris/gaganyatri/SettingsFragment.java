package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private ConstraintLayout coTravellerBtn; // Make sure to keep it as an instance variable

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false); // Inflate and get the View

        coTravellerBtn = view.findViewById(R.id.co_traveller_btn);

        if (coTravellerBtn != null) {
            coTravellerBtn.setOnClickListener(v -> {
                Intent i = new Intent(requireContext(), CoTravellerActivity.class);
                startActivity(i);
            });
        }

        return view; // Return the inflated view
    }
}