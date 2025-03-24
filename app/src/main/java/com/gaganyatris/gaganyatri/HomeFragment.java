package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class HomeFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private TextView heyTextView;

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
        LinearLayout planYourTrip = view.findViewById(R.id.planYourTrip);
        heyTextView = view.findViewById(R.id.hey);
        view.findViewById(R.id.cameraLens).setOnClickListener(v-> startActivity(new Intent(requireContext(), ImageSearchActivity.class)));
        view.findViewById(R.id.gagoo).setOnClickListener(v->startActivity(new Intent(requireContext(), AiChatBotActivity.class)));

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            loadUserData(currentUser.getUid()); // Load user's first name
        }

        findTickets.setOnClickListener(v -> startActivity(new Intent(requireContext(), ModeOfTransportActivity.class)));
        travelGroups.setOnClickListener(v -> startActivity(new Intent(requireContext(), TravelGroupActivity.class)));
        getGuide.setOnClickListener(v -> startActivity(new Intent(requireContext(), GetGuideActivity.class)));
        planYourTrip.setOnClickListener(v -> startActivity(new Intent(requireContext(), PlanYourTripActivity.class)));
        view.findViewById(R.id.exploreLocation).setOnClickListener(v -> startActivity(new Intent(requireContext(), ExploreYourLocationActivity.class)));
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

                    if (name != null && !name.isEmpty()) {
                        String firstName = name.split("\\s+")[0]; // Get the first word before whitespace
                        heyTextView.setText("Hey 👋 " + firstName);
                    } else {
                        heyTextView.setText("Hey 👋 Traveler"); // Default if name is null or empty
                    }
                }
            }
        });
    }
}
