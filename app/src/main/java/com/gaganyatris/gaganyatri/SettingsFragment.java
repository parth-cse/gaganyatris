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
import androidx.fragment.app.Fragment;

import com.gaganyatris.gaganyatri.models.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private TextView fnameField, pNoField;
    private ImageView avatarImage;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private final int[] avatarResources = {
            R.drawable.ic_avatar_r1, R.drawable.ic_avatar_r2, R.drawable.ic_avatar_r3,
            R.drawable.ic_avatar_r4, R.drawable.ic_avatar_r5, R.drawable.ic_avatar_r6,
            R.drawable.ic_avatar_r7, R.drawable.ic_avatar_r8, R.drawable.ic_avatar_r9
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize UI elements
        fnameField = view.findViewById(R.id.fname_field);
        pNoField = view.findViewById(R.id.pNoField);
        avatarImage = view.findViewById(R.id.avatarImage); // Make sure it's an ImageView in XML

        if (currentUser != null) {
            loadUserData(currentUser.getUid()); // Load user data from Firestore
        }

        // Set up button actions
        view.findViewById(R.id.button).setOnClickListener(v -> startActivity(new Intent(requireContext(), UserDetailActivity.class)));
        view.findViewById(R.id.co_traveller_btn).setOnClickListener(v -> startActivity(new Intent(requireContext(), CoTravellerActivity.class)));
        view.findViewById(R.id.logout).setOnClickListener(v -> signOut());
        view.findViewById(R.id.change_language).setOnClickListener(v -> startActivity(new Intent(requireContext(), ChangeLanguageActivity.class)));
        view.findViewById(R.id.changeUI).setOnClickListener(v -> startActivity(new Intent(requireContext(), UiModeActivity.class)));
        view.findViewById(R.id.travel_settings).setOnClickListener(v -> startActivity(new Intent(requireContext(), TravelSettingsActivity.class)));
        view.findViewById(R.id.help).setOnClickListener(v -> startActivity(new Intent(requireContext(), HelpSupportActivity.class)));

        return view;
    }

    private void loadUserData(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(requireContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String name = snapshot.getString("name");
                    String phone = snapshot.getString("phone");
                    Long avatarIndexLong = snapshot.getLong("avatarIndex"); // Fetch avatar index
                    Object dobObject = snapshot.get("dateOfBirth"); // Fetch DOB

                    String dateOfBirth = "";
                    if (dobObject instanceof com.google.firebase.Timestamp) {
                        dateOfBirth = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(((com.google.firebase.Timestamp) dobObject).toDate());
                    } else if (dobObject instanceof String) {
                        dateOfBirth = (String) dobObject;
                    }

                    fnameField.setText(name != null ? name : "User");
                    pNoField.setText(phone != null ? phone : "+91 XXXXXXXXXX");

                    // Debugging log
                    System.out.println("User DOB: " + dateOfBirth);

                    // Set avatar image
                    if (avatarIndexLong != null) {
                        int avatarIndex = avatarIndexLong.intValue();
                        if (avatarIndex >= 0 && avatarIndex < avatarResources.length) {
                            avatarImage.setImageResource(avatarResources[avatarIndex]);
                        }
                    }
                }
            }
        });
    }


    private void signOut() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(requireContext(), OnBoardingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
        Toast.makeText(requireContext(), "Logged Out Successfully", Toast.LENGTH_SHORT).show();
    }
}
