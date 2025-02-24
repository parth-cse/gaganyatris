package com.gaganyatris.gaganyatri;

import android.content.Intent;
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

import com.gaganyatris.gaganyatri.models.Users;
import com.gaganyatris.gaganyatri.utils.LoadingDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SelectAvatarFragment extends Fragment {

    private CardView selectedCardView = null;
    private int selectedAvatarIndex = -1;
    private CardView[] cardViews;
    private LoadingDialog loadingDialog;

    public SelectAvatarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_avatar, container, false);

        LinearLayout btnBack = view.findViewById(R.id.btn_back);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                FragmentManager fm = requireActivity().getSupportFragmentManager();
                fm.popBackStack();
            });
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardViews = new CardView[]{
                view.findViewById(R.id.cardView1), view.findViewById(R.id.cardView2), view.findViewById(R.id.cardView3),
                view.findViewById(R.id.cardView11), view.findViewById(R.id.cardView12), view.findViewById(R.id.cardView13),
                view.findViewById(R.id.cardView21), view.findViewById(R.id.cardView22), view.findViewById(R.id.cardView23)
        };

        // Set the first card as the default selected one
        selectAvatar(cardViews[0], 0);

        for (int i = 0; i < cardViews.length; i++) {
            final int index = i;
            cardViews[i].setOnClickListener(v -> selectAvatar(cardViews[index], index));
        }

        Button btnSave = view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> {
            if (selectedAvatarIndex != -1) {
                // Get data from arguments
                Bundle args = getArguments();
                if (args != null) {
                    String name = args.getString("name");
                    String email = args.getString("email");
                    String phone = args.getString("phone");
                    String dob = args.getString("dob");
                    Timestamp dobTimestamp = null;
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        Date date = sdf.parse(dob);
                        if (date != null) {
                            dobTimestamp = new Timestamp(date);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    String gender = args.getString("gender");
                    String country = args.getString("country");
                    String state = args.getString("state");
                    String city = args.getString("city");

                    // Get current Firebase User
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();

                        // Create a Users object
                        Users userData = new Users(uid, name, email, phone, dobTimestamp, gender, country, state, city, selectedAvatarIndex);

                        // Save to Firestore
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        loadingDialog = new LoadingDialog(requireContext());
                        loadingDialog.setMessage("Saving Data...");
                        loadingDialog.show();

                        db.collection("users").document(uid).set(userData)
                                .addOnSuccessListener(documentReference -> {
                                    if (loadingDialog != null && loadingDialog.isShowing()) {
                                        loadingDialog.dismiss();
                                    }

                                    // Navigate back based on the calling activity
                                    if (getActivity() instanceof OTPVerificationActivity) {
                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the back stack
                                        startActivity(intent);
                                    } else {
                                        // If called from another activity, simply finish the current activity
                                        if (getActivity() != null) {
                                            getActivity().finish();
                                        }
                                    }

                                })
                                .addOnFailureListener(e -> {
                                    if (loadingDialog != null && loadingDialog.isShowing()) {
                                        loadingDialog.dismiss();
                                    }
                                    Toast.makeText(getContext(), "Error saving user data", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                    }
                }

            } else {
                Toast.makeText(getContext(), "Please select an avatar", Toast.LENGTH_SHORT).show();
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

        //Change all other cardview color to white.
        for (CardView otherCard : cardViews) {
            if (otherCard != cardView) {
                otherCard.setCardBackgroundColor(Color.WHITE);
            }
        }
    }
}