package com.gaganyatris.gaganyatri;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;

public class PersonalDetailsFragment extends Fragment {

    public PersonalDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_personal_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AutoCompleteTextView autoCompleteTextView = view.findViewById(R.id.autoCompleteTextView);
        Button btnSaveNext = view.findViewById(R.id.btn_save_next);

        // Allowed values
        String[] genderOptions = {"Male", "Female", "Other"};

        // Create an adapter with the allowed values
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, genderOptions);

        // Set adapter to AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter);

        // Ensure user selects from dropdown (disables manual input)
        autoCompleteTextView.setKeyListener(null);

        btnSaveNext.setOnClickListener(v -> {
            // Use a unique tag for the fragment transaction
            String tag = "selectAvatarFragment";

            // Check if the fragment already exists on the back stack
            FragmentManager fm = requireActivity().getSupportFragmentManager();
            Fragment existingFragment = fm.findFragmentByTag(tag);

            FragmentTransaction transaction = fm.beginTransaction();

            if (existingFragment == null) {
                // Create a new instance only if it doesn't exist
                existingFragment = new SelectAvatarFragment();
                transaction.replace(R.id.fragment_container, existingFragment, tag); // Add tag here
                transaction.addToBackStack(tag); // Add tag to back stack
            } else {
                // If it exists, simply show it (no need to replace)
                transaction.show(existingFragment); // If hidden, show it
            }


            transaction.commit();

        });
    }
}
