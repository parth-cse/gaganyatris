package com.gaganyatris.gaganyatri;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.gaganyatris.gaganyatri.models.CoTraveller;
import com.gaganyatris.gaganyatri.utils.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectCoTravellerFragment extends Fragment {

    private LoadingDialog loader;
    Button saveNext;
    private LinearLayout coTravellerContainer;
    private String startDate, endDate, from, to, budgetAmount, tripType;
    private boolean exploreNearby;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Switch exploreSwitch;
    private final Set<String> selectedCoTravellers = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_co_traveller, container, false);

        saveNext = view.findViewById(R.id.btn_save);
        coTravellerContainer = view.findViewById(R.id.coTravellerContainer);
        LinearLayout btnBack = view.findViewById(R.id.btn_back);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        }

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        loader = new LoadingDialog(requireContext());
        loadCoTravellers();

        return view;
    }

    private void loadCoTravellers() {
        loader.setMessage("Loading Co-Travellers...");
        loader.show();
        String userId = auth.getCurrentUser().getUid();
        db.collection("coTravellers")
                .whereEqualTo("user_uid", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    coTravellerContainer.removeAllViews();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        CoTraveller coTraveller = document.toObject(CoTraveller.class);
                        coTraveller.setCoTraveller_id(document.getId());
                        addCoTravellerCard(coTraveller);
                    }
                    loader.dismiss();

                    // Apply selection state after loading co-travellers
                    if (exploreSwitch != null && exploreSwitch.isChecked()) {
                        toggleCoTravellerSelection(true);
                    }
                })
                .addOnFailureListener(e -> {
                    loader.dismiss();
                    Toast.makeText(getContext(), "Failed to load co-travellers", Toast.LENGTH_SHORT).show();
                });
    }


    private void addCoTravellerCard(CoTraveller coTraveller) {
        View cardView = getLayoutInflater().inflate(R.layout.select_co_traveller_card, coTravellerContainer, false);

        TextView nameTextView = cardView.findViewById(R.id.textView4);
        TextView emailTextView = cardView.findViewById(R.id.some_id);
        ConstraintLayout cardViewmain = cardView.findViewById(R.id.main);
        CardView card = cardView.findViewById(R.id.cardView);


        nameTextView.setText(coTraveller.getName());
        emailTextView.setText(coTraveller.getEmail());
        int avatarResource = getAvatarDrawable(coTraveller.getAvatarIndex());
        if (avatarResource != 0) {
            card.setForeground(ContextCompat.getDrawable(requireContext(), avatarResource));
        }

        cardViewmain.setOnClickListener(v -> {
            String coTravellerId = coTraveller.getCoTraveller_id();
            if (selectedCoTravellers.contains(coTravellerId)) {
                selectedCoTravellers.remove(coTravellerId);
                cardViewmain.setBackgroundResource(R.drawable.card_settings);
            } else {
                selectedCoTravellers.add(coTravellerId);
                cardViewmain.setBackgroundResource(R.drawable.card_language_active);
            }

        });

        coTravellerContainer.addView(cardView);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        exploreSwitch = view.findViewById(R.id.explore);
        exploreSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> toggleCoTravellerSelection(isChecked));

        if (exploreSwitch.isChecked()) {
            toggleCoTravellerSelection(true);
        }

        if (getArguments() != null) {
            startDate = getArguments().getString("tripStartDate");
            endDate = getArguments().getString("tripEndDate");
            from = getArguments().getString("from");
            to = getArguments().getString("to");
            budgetAmount = getArguments().getString("budget");
            tripType = getArguments().getString("tripType");
            exploreNearby = getArguments().getBoolean("exploreNearby");
        }

        saveNext.setOnClickListener(v -> {
            ArrayList<String> selectedIds = new ArrayList<>(selectedCoTravellers);
            Bundle bundle = new Bundle();
            bundle.putString("tripStartDate", startDate);
            bundle.putString("tripEndDate", endDate);
            bundle.putString("from", from);
            bundle.putString("to", to);
            bundle.putString("budget", budgetAmount);
            bundle.putString("tripType", tripType);
            bundle.putBoolean("exploreNearby", exploreNearby);
            bundle.putStringArrayList("selectedCoTravellers", selectedIds);

            TravelModeFragment travelModeFragment = new TravelModeFragment();
            travelModeFragment.setArguments(bundle);

            FragmentManager fm = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragmentContainer, travelModeFragment, "travelModeFragment");
            transaction.addToBackStack("travelModeFragment");
            transaction.commit();
        });
    }

    private int getAvatarDrawable(int avatarIndex) {
        switch (avatarIndex) {
            case 0: return R.drawable.ic_avatar_r1;
            case 1: return R.drawable.ic_avatar_r2;
            case 2: return R.drawable.ic_avatar_r3;
            case 3: return R.drawable.ic_avatar_r4;
            case 4: return R.drawable.ic_avatar_r5;
            case 5: return R.drawable.ic_avatar_r6;
            case 6: return R.drawable.ic_avatar_r7;
            case 7: return R.drawable.ic_avatar_r8;
            case 8: return R.drawable.ic_avatar_r9;
            default: return 0;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (exploreSwitch != null) {
            exploreSwitch.post(() -> toggleCoTravellerSelection(exploreSwitch.isChecked()));
        }
    }


    private void toggleCoTravellerSelection(boolean isSolo) {
        for (int i = 0; i < coTravellerContainer.getChildCount(); i++) {
            View cardView = coTravellerContainer.getChildAt(i);
            cardView.setEnabled(!isSolo);
            cardView.setAlpha(isSolo ? 0.5f : 1.0f);
        }

        if (isSolo) {
            selectedCoTravellers.clear();
        }
    }


}