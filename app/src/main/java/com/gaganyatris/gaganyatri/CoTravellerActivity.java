package com.gaganyatris.gaganyatri;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gaganyatris.gaganyatri.models.CoTraveller;
import com.gaganyatris.gaganyatri.utils.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class CoTravellerActivity extends AppCompatActivity {

    private LinearLayout coTravellerContainer;
    private FirebaseFirestore db;
    private LoadingDialog loadingDialog;
    private static final String TAG = "CoTravellerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_co_traveller);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.add_co_traveller).setOnClickListener(v -> {startActivity(new Intent(CoTravellerActivity.this, AddCoTravellerActivity.class)); finish();});

        coTravellerContainer = findViewById(R.id.coTravellerContainer);
        db = FirebaseFirestore.getInstance();

        setupLoadingDialog(); // Initialize the loading dialog
        loadBaseUserDetails();
        loadCoTravellers();
    }

    private void setupLoadingDialog() {
        loadingDialog = new LoadingDialog(this);
        loadingDialog.setMessage("Please Wait...");
        loadingDialog.show();
    }

    private void loadCoTravellers() {
        loadingDialog.show(); // Show loading dialog

        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("coTravellers")
                .whereEqualTo("user_uid", userUid)
                .get()
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss(); // Dismiss dialog after fetching data

                    if (task.isSuccessful()) {
                        coTravellerContainer.removeAllViews(); // Clear old data

                        if (task.getResult().isEmpty()) {
                            Toast.makeText(this, "No co-travellers found!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            CoTraveller coTraveller = document.toObject(CoTraveller.class);
                            addCoTravellerView(coTraveller);
                        }
                    } else {
                        Log.e(TAG, "Error getting co-travellers", task.getException());
                        Toast.makeText(this, "Failed to load co-travellers", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addCoTravellerView(CoTraveller coTraveller) {
        View coTravellerView = LayoutInflater.from(this).inflate(R.layout.co_traveller_card, coTravellerContainer, false);

        TextView nameTextView = coTravellerView.findViewById(R.id.textView4);
        TextView emailTextView = coTravellerView.findViewById(R.id.some_id);
        CardView avatarCard = coTravellerView.findViewById(R.id.cardView);

        nameTextView.setText(coTraveller.getName());
        emailTextView.setText(coTraveller.getEmail());

        int avatarResource = getAvatarDrawable(coTraveller.getAvatarIndex());
        if (avatarResource != 0) {
            avatarCard.setForeground(ContextCompat.getDrawable(this, avatarResource));
        }

        coTravellerContainer.addView(coTravellerView);
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

    private void loadBaseUserDetails() {
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userUid) // Assuming user data is stored in "users" collection
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        int avatarIndex = documentSnapshot.getLong("avatarIndex").intValue(); // Ensure it is an integer

                        // Update UI
                        TextView nameTextView = findViewById(R.id.textView4);
                        TextView emailTextView = findViewById(R.id.some_id);
                        CardView avatarCard = findViewById(R.id.cardView);

                        nameTextView.setText(name);
                        emailTextView.setText(email);

                        int avatarResource = getAvatarDrawable(avatarIndex);
                        if (avatarResource != 0) {
                            avatarCard.setForeground(ContextCompat.getDrawable(this, avatarResource));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user details", Toast.LENGTH_SHORT).show();
                });
    }

}
