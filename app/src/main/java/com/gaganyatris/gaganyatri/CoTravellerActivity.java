package com.gaganyatris.gaganyatri;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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
    final int statusBarColor = R.color.newStatusBar;

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
        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));
        findViewById(R.id.add_co_traveller).setOnClickListener(v -> startActivity(new Intent(CoTravellerActivity.this, AddCoTravellerActivity.class)));
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        coTravellerContainer = findViewById(R.id.coTravellerContainer);
        db = FirebaseFirestore.getInstance();

        setupLoadingDialog(); // Initialize the loading dialog
        loadBaseUserDetails();
        loadCoTravellers();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        ImageButton optionsButton = coTravellerView.findViewById(R.id.optionsLayout);

        nameTextView.setText(coTraveller.getName());
        emailTextView.setText(coTraveller.getEmail());

        int avatarResource = getAvatarDrawable(coTraveller.getAvatarIndex());
        if (avatarResource != 0) {
            avatarCard.setForeground(ContextCompat.getDrawable(this, avatarResource));
        }

        // Set up popup menu for Edit/Delete
        optionsButton.setOnClickListener(v -> showPopupMenu(v, coTraveller));

        coTravellerContainer.addView(coTravellerView);
    }

    private void showPopupMenu(View view, CoTraveller coTraveller) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.co_traveller_menu); // Ensure this menu XML exists

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId(); // Extract item ID first

            if (itemId == R.id.menu_edit) {
                // editCoTraveller(coTraveller);
                Intent intent = new Intent(CoTravellerActivity.this, AddCoTravellerActivity.class);
                intent.putExtra("coTraveller_id", coTraveller.getCoTraveller_id()); // Pass coTraveller_id
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_delete) {
                deleteCoTraveller(coTraveller.getCoTraveller_id(), view);
                loadCoTravellers();
                return true;
            } else {
                return false;
            }
        });

        popupMenu.show();
    }

    private void deleteCoTraveller(String coTravellerId, View coTravellerView) {
        db.collection("coTravellers").document(coTravellerId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    coTravellerContainer.removeView(coTravellerView);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
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
