package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class SettingsFragment extends Fragment {

    private TextView nameTextView, phoneTextView;
    private ImageView avatarImageView;
    private FirebaseFirestore firestoreDB;
    private FirebaseUser currentUser;
    private ListenerRegistration firestoreListener;

    private static final int[] AVATAR_RESOURCES = {
            R.drawable.ic_avatar_r1, R.drawable.ic_avatar_r2, R.drawable.ic_avatar_r3,
            R.drawable.ic_avatar_r4, R.drawable.ic_avatar_r5, R.drawable.ic_avatar_r6,
            R.drawable.ic_avatar_r7, R.drawable.ic_avatar_r8, R.drawable.ic_avatar_r9
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        initialize(view);
        setupClickListeners(view);
        fetchUserData();
        return view;
    }

    private void initialize(View view) {
        firestoreDB = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        nameTextView = view.findViewById(R.id.fname_field);
        phoneTextView = view.findViewById(R.id.pNoField);
        avatarImageView = view.findViewById(R.id.avatarImage);
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.button).setOnClickListener(v -> navigate(UserDetailActivity.class));
        view.findViewById(R.id.co_traveller_btn).setOnClickListener(v -> navigate(CoTravellerActivity.class));
        view.findViewById(R.id.logout).setOnClickListener(v -> signOut());
        view.findViewById(R.id.change_language).setOnClickListener(v -> navigate(ChangeLanguageActivity.class));
        view.findViewById(R.id.changeUI).setOnClickListener(v -> navigate(UiModeActivity.class));
        view.findViewById(R.id.travel_settings).setOnClickListener(v -> navigate(TravelSettingsActivity.class));
        view.findViewById(R.id.help).setOnClickListener(v -> navigate(HelpSupportActivity.class));
    }

    private void fetchUserData() {
        if (currentUser == null) return;
        DocumentReference userRef = firestoreDB.collection("users").document(currentUser.getUid());
        firestoreListener = userRef.addSnapshotListener(this::handleUserDataSnapshot);
    }

    private void handleUserDataSnapshot(@Nullable DocumentSnapshot snapshot, @Nullable Exception error) {
        if (error != null) {
            showToast("Error loading user data");
            return;
        }

        if (snapshot != null && snapshot.exists() && isAdded()) {
            nameTextView.setText(snapshot.getString("name") != null ? snapshot.getString("name") : "User");
            phoneTextView.setText(snapshot.getString("phone") != null ? snapshot.getString("phone") : "+91 XXXXXXXXXX");
            updateAvatar(snapshot.getLong("avatarIndex"));
        }
    }

    private void updateAvatar(Long avatarIndexLong) {
        if (avatarIndexLong != null) {
            int avatarIndex = avatarIndexLong.intValue();
            if (avatarIndex >= 0 && avatarIndex < AVATAR_RESOURCES.length) {
                avatarImageView.setImageResource(AVATAR_RESOURCES[avatarIndex]);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firestoreListener != null) {
            firestoreListener.remove();
            firestoreListener = null;
        }
    }

    private void signOut() {
        if (firestoreListener != null) {
            firestoreListener.remove();
            firestoreListener = null;
        }

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(requireContext(), OnBoardingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
        showToast("Logged Out Successfully");
    }

    private void navigate(Class<?> destination) {
        startActivity(new Intent(requireContext(), destination));
    }

    private void showToast(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}