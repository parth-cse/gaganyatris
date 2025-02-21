package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class HistoryFragment extends Fragment {

    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        View historyCard = view.findViewById(R.id.historyCard);
        if (historyCard != null) {
            historyCard.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), TripDetailsActivity.class);
                startActivity(intent);
            });
        } else {
            Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show();
        }



        return view;
    }
}