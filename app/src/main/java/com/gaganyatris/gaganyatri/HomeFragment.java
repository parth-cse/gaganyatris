package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        LinearLayout findTickets = view.findViewById(R.id.find_tickets);
        LinearLayout travelGroups = view.findViewById(R.id.travel_groups);
        LinearLayout getGuide = view.findViewById(R.id.get_guide);

        findTickets.setOnClickListener(v -> startActivity(new Intent(requireContext(), ModeOfTransportActivity.class)));
        travelGroups.setOnClickListener(v -> startActivity(new Intent(requireContext(), TravelGroupActivity.class)));
        getGuide.setOnClickListener(v -> startActivity(new Intent(requireContext(), GetGuideActivity.class)));

        return view;
    }
}