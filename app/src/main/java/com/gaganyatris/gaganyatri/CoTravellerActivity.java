package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

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
    private static final int STATUS_BAR_COLOR = R.color.newStatusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_co_traveller);
        setupWindowInsets();
        getWindow().setStatusBarColor(ContextCompat.getColor(this, STATUS_BAR_COLOR));
        initializeViews();
        setupListeners();
        loadBaseUserDetails();
        loadCoTravellers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCoTravellers();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        coTravellerContainer = findViewById(R.id.coTravellerContainer);
        db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(this);
        loadingDialog.setMessage("Please Wait...");
    }

    private void setupListeners() {
        findViewById(R.id.add_co_traveller).setOnClickListener(v -> startActivity(new Intent(this, AddCoTravellerActivity.class)));
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
    }

    private void loadCoTravellers() {
        loadingDialog.show();
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("coTravellers")
                .whereEqualTo("user_uid", userUid)
                .get()
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();
                    if (task.isSuccessful()) {
                        coTravellerContainer.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            addCoTravellerView(document.toObject(CoTraveller.class));
                        }
                    } else {
                        Log.e(TAG, "Error getting co-travellers", task.getException());
                    }
                });
    }

    private void addCoTravellerView(CoTraveller coTraveller) {
        View view = LayoutInflater.from(this).inflate(R.layout.co_traveller_card, coTravellerContainer, false);
        ((TextView) view.findViewById(R.id.textView4)).setText(coTraveller.getName());
        ((TextView) view.findViewById(R.id.some_id)).setText(coTraveller.getEmail());
        CardView avatarCard = view.findViewById(R.id.cardView);
        avatarCard.setForeground(ContextCompat.getDrawable(this, getAvatarDrawable(coTraveller.getAvatarIndex())));
        view.findViewById(R.id.optionsLayout).setOnClickListener(v -> showPopupMenu(v, coTraveller));
        coTravellerContainer.addView(view);
    }

    private void showPopupMenu(View view, CoTraveller coTraveller) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.co_traveller_menu);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_edit) {
                Intent intent = new Intent(this, AddCoTravellerActivity.class);
                intent.putExtra("coTraveller_id", coTraveller.getCoTraveller_id());
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.menu_delete) {
                deleteCoTraveller(coTraveller.getCoTraveller_id(), view);
                loadCoTravellers();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void deleteCoTraveller(String id, View view) {
        db.collection("coTravellers").document(id).delete()
                .addOnSuccessListener(aVoid -> coTravellerContainer.removeView(view))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete co-traveller", e));
    }

    private int getAvatarDrawable(int index) {
        switch (index) {
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
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        ((TextView) findViewById(R.id.textView4)).setText(doc.getString("name"));
                        ((TextView) findViewById(R.id.some_id)).setText(doc.getString("email"));
                        ((CardView) findViewById(R.id.cardView)).setForeground(ContextCompat.getDrawable(this, getAvatarDrawable(doc.getLong("avatarIndex").intValue())));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load user details", e));
    }
}