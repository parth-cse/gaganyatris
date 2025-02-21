package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.gaganyatris.gaganyatri.utils.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Use a more concise way to get views and set click listeners
        // Also, use Kotlin's apply scope function for cleaner code.
        view.findViewById(R.id.button).setOnClickListener(v -> startActivity(new Intent(requireContext(), UserDetailActivity.class)));
        view.findViewById(R.id.co_traveller_btn).setOnClickListener(v -> startActivity(new Intent(requireContext(), CoTravellerActivity.class)));
        view.findViewById(R.id.logout).setOnClickListener(v -> showLogoutLoader());
        view.findViewById(R.id.change_language).setOnClickListener(v -> startActivity(new Intent(requireContext(), ChangeLanguageActivity.class)));
        view.findViewById(R.id.changeUI).setOnClickListener(v -> startActivity(new Intent(requireContext(), UiModeActivity.class)));
        view.findViewById(R.id.travel_settings).setOnClickListener(v -> startActivity(new Intent(requireContext(), TravelSettingsActivity.class)));
        view.findViewById(R.id.help).setOnClickListener(v -> startActivity(new Intent(requireContext(), HelpSupportActivity.class)));

        return view;
    }

    private void showLogoutLoader() {
        LoadingDialog progressDialog = new LoadingDialog(requireContext());
        progressDialog.setMessage("Logging out...");
        progressDialog.show();

        // Use a shorter delay if 2 seconds is too long. Consider making it configurable.
        new Handler().postDelayed(() -> {
            signOut(progressDialog); // Pass the dialog to the signOut method
        }, 1000); // 1-second delay
    }

    private void signOut(LoadingDialog progressDialog) {
        mAuth.signOut();
        progressDialog.dismiss(); // Dismiss the dialog immediately after signing out.

        Intent intent = new Intent(requireContext(), OnBoardingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
        Toast.makeText(requireContext(), "Logged Out Successfully", Toast.LENGTH_SHORT).show();
    }
}