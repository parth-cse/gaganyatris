package com.gaganyatris.gaganyatri;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.gaganyatris.gaganyatri.utils.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        ConstraintLayout coTravellerBtn = view.findViewById(R.id.co_traveller_btn);
        ConstraintLayout logoutBTN = view.findViewById(R.id.logout);

        if (coTravellerBtn != null) {
            coTravellerBtn.setOnClickListener(v -> {
                Intent i = new Intent(requireContext(), CoTravellerActivity.class);
                startActivity(i);
            });
        }

        if (logoutBTN != null) {
            logoutBTN.setOnClickListener(view1 -> showLogoutLoader());
        }

        return view;
    }

    private void showLogoutLoader() {
        LoadingDialog progressDialog = new LoadingDialog(requireContext());
        progressDialog.setMessage("Logging out...");
        progressDialog.show();

        new Handler().postDelayed(() -> {
            signOut();
            progressDialog.dismiss();
        }, 2000); // 2-second delay
    }

    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(requireContext(), OnBoardingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
        Toast.makeText(requireContext(), "Logged Out Successfully", Toast.LENGTH_SHORT).show();
    }
}
