package com.gaganyatris.gaganyatri;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.gaganyatris.gaganyatri.models.CoTraveller;
import com.gaganyatris.gaganyatri.utils.LoadingDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CoTravellerAvatarFragment extends Fragment {

    private CardView selectedCardView = null;
    private int selectedAvatarIndex = -1;
    private CardView[] cardViews;
    private LoadingDialog loadingDialog;
    private CoTraveller coTraveller;
    private Button btnSave;
    private boolean isUpdating = false; // Track if editing existing co-traveller

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_avatar, container, false);

        LinearLayout btnBack = view.findViewById(R.id.btn_back);
        btnSave = view.findViewById(R.id.btn_save);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                FragmentManager fm = requireActivity().getSupportFragmentManager();
                fm.popBackStack();
            });
        }

        // Retrieve coTraveller object from arguments
        Bundle args = getArguments();
        if (args != null) {
            coTraveller = args.getParcelable("coTraveller");
            boolean isEditing = args.getBoolean("isEditing", false);

            // **Update button text immediately**
            if (isEditing) {
                btnSave.setText("Update");
            } else {
                btnSave.setText("Save");
            }

        }

        cardViews = new CardView[]{
                view.findViewById(R.id.cardView1), view.findViewById(R.id.cardView2), view.findViewById(R.id.cardView3),
                view.findViewById(R.id.cardView11), view.findViewById(R.id.cardView12), view.findViewById(R.id.cardView13),
                view.findViewById(R.id.cardView21), view.findViewById(R.id.cardView22), view.findViewById(R.id.cardView23)
        };

        // Set default avatar selection
        selectAvatar(cardViews[0], 0);

        for (int i = 0; i < cardViews.length; i++) {
            final int index = i;
            cardViews[i].setOnClickListener(v -> selectAvatar(cardViews[index], index));
        }

        // Check if the coTraveller already exists in Firestore
        if (coTraveller != null) {
            checkIfCoTravellerExists(coTraveller.getCoTraveller_id());
        }

        btnSave.setOnClickListener(v -> {
            if (coTraveller != null) {
                coTraveller.setAvatarIndex(selectedAvatarIndex);

                // Save or update data in Firestore
                if (isUpdating) {
                    updateCoTravellerInFirestore(coTraveller);
                } else {
                    saveCoTravellerToFirestore(coTraveller);
                }
            } else {
                Toast.makeText(getContext(), "No Co-Traveller data found!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void selectAvatar(CardView cardView, int index) {
        if (selectedCardView != null) {
            selectedCardView.setCardBackgroundColor(Color.WHITE);
        }
        cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.holo_green_dark));
        selectedCardView = cardView;
        selectedAvatarIndex = index;

        // Change all other card views to white
        for (CardView otherCard : cardViews) {
            if (otherCard != cardView) {
                otherCard.setCardBackgroundColor(Color.WHITE);
            }
        }
    }

    private void checkIfCoTravellerExists(String coTravellerId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("coTravellers").document(coTravellerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Co-traveller already exists, update UI accordingly
                        isUpdating = true;
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error checking data!", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveCoTravellerToFirestore(CoTraveller coTraveller) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        loadingDialog = new LoadingDialog(requireContext());
        loadingDialog.setMessage("Saving Data...");
        loadingDialog.show();

        db.collection("coTravellers").document(coTraveller.getCoTraveller_id())
                .set(coTraveller)
                .addOnSuccessListener(documentReference -> {
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                    requireActivity().finish();
                })
                .addOnFailureListener(e -> {
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                    Toast.makeText(getContext(), "Error saving data!", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCoTravellerInFirestore(CoTraveller coTraveller) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        loadingDialog = new LoadingDialog(requireContext());
        loadingDialog.setMessage("Updating Data...");
        loadingDialog.show();

        db.collection("coTravellers").document(coTraveller.getCoTraveller_id())
                .update("avatarIndex", coTraveller.getAvatarIndex())
                .addOnSuccessListener(aVoid -> {
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                    requireActivity().finish();
                })
                .addOnFailureListener(e -> {
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                    Toast.makeText(getContext(), "Error updating data!", Toast.LENGTH_SHORT).show();
                });
    }
}
