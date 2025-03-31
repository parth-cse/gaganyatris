package com.gaganyatris.gaganyatri;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.gaganyatris.gaganyatri.models.CoTraveller;
import com.gaganyatris.gaganyatri.utils.LoadingDialog;
import com.google.firebase.firestore.FirebaseFirestore;

public class CoTravellerAvatarFragment extends Fragment {

    private CardView selectedCardView = null;
    private int selectedAvatarIndex = -1;
    private CardView[] cardViews;
    private LoadingDialog loadingDialog;
    private CoTraveller coTraveller;
    private Button btnSave;
    private boolean isUpdating = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_avatar, container, false);
        initializeViews(view);
        setupBackNavigation(view);
        retrieveCoTravellerData();
        setupAvatarSelection();
        setupSaveButton();
        return view;
    }

    private void initializeViews(View view) {
        btnSave = view.findViewById(R.id.btn_save);
        cardViews = new CardView[]{
                view.findViewById(R.id.cardView1), view.findViewById(R.id.cardView2), view.findViewById(R.id.cardView3),
                view.findViewById(R.id.cardView11), view.findViewById(R.id.cardView12), view.findViewById(R.id.cardView13),
                view.findViewById(R.id.cardView21), view.findViewById(R.id.cardView22), view.findViewById(R.id.cardView23)
        };
    }

    private void setupBackNavigation(View view) {
        LinearLayout btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        }
    }

    private void retrieveCoTravellerData() {
        Bundle args = getArguments();
        if (args != null) {
            coTraveller = args.getParcelable("coTraveller");
            isUpdating = args.getBoolean("isEditing", false);
            btnSave.setText(isUpdating ? "Update" : "Save");
            if (coTraveller != null) {
                checkIfCoTravellerExists(coTraveller.getCoTraveller_id());
            }
        }
    }

    private void setupAvatarSelection() {
        selectAvatar(cardViews[0], 0);
        for (int i = 0; i < cardViews.length; i++) {
            final int index = i;
            cardViews[i].setOnClickListener(v -> selectAvatar(cardViews[index], index));
        }
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            if (coTraveller != null) {
                coTraveller.setAvatarIndex(selectedAvatarIndex);
                saveOrUpdateCoTraveller();
            } else {
                Toast.makeText(getContext(), "No Co-Traveller data found!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectAvatar(CardView cardView, int index) {
        if (selectedCardView != null) {
            selectedCardView.setCardBackgroundColor(Color.WHITE);
        }
        cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.holo_green_dark));
        selectedCardView = cardView;
        selectedAvatarIndex = index;
        for (CardView otherCard : cardViews) {
            if (otherCard != cardView) {
                otherCard.setCardBackgroundColor(Color.WHITE);
            }
        }
    }

    private void checkIfCoTravellerExists(String coTravellerId) {
        FirebaseFirestore.getInstance().collection("coTravellers").document(coTravellerId).get()
                .addOnSuccessListener(documentSnapshot -> isUpdating = documentSnapshot.exists())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error checking data!", Toast.LENGTH_SHORT).show());
    }

    private void saveOrUpdateCoTraveller() {
        loadingDialog = new LoadingDialog(requireContext());
        loadingDialog.setMessage(isUpdating ? "Updating Data..." : "Saving Data...");
        loadingDialog.show();

        FirebaseFirestore.getInstance().collection("coTravellers").document(coTraveller.getCoTraveller_id())
                .set(coTraveller)
                .addOnSuccessListener(aVoid -> handleSuccess())
                .addOnFailureListener(e -> handleError());
    }

    private void handleSuccess() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        requireActivity().finish();
    }

    private void handleError() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        Toast.makeText(getContext(), "Error " + (isUpdating ? "updating" : "saving") + " data!", Toast.LENGTH_SHORT).show();
    }
}