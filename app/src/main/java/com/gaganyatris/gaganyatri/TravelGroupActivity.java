package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gaganyatris.gaganyatri.utils.LoadingDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TravelGroupActivity extends AppCompatActivity {

    private LinearLayout container;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_travel_group);
        setupWindowInsets();
        setupStatusBarColor();
        initializeLoadingDialog();
        setupBackButton();
        initializeContainer();
        fetchTravelGroups();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupStatusBarColor() {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.newStatusBar));
    }

    private void initializeLoadingDialog() {
        loadingDialog = new LoadingDialog(this);
        loadingDialog.setMessage("Please Wait...");
        loadingDialog.show();
    }

    private void setupBackButton() {
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
    }

    private void initializeContainer() {
        container = findViewById(R.id.container);
        container.removeAllViews();
    }

    private void fetchTravelGroups() {
        FirebaseFirestore.getInstance().collection("travel_groups")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingDialog.dismiss();
                    queryDocumentSnapshots.forEach(this::inflateTravelGroupCard);
                })
                .addOnFailureListener(e -> {
                    // Handle failure (e.g., show a toast or log the error)
                });
    }

    private void inflateTravelGroupCard(DocumentSnapshot document) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.travel_group_card, container, false);

        TextView dateTextView = cardView.findViewById(R.id.textView26);
        TextView daysTextView = cardView.findViewById(R.id.textView27);
        TextView tripNameTextView = cardView.findViewById(R.id.textView28);
        TextView priceTextView = cardView.findViewById(R.id.textView30);
        TextView organiserTextView = cardView.findViewById(R.id.textView29);
        ImageView tripImageView = cardView.findViewById(R.id.img);

        populateCardData(document, dateTextView, daysTextView, tripNameTextView, priceTextView, organiserTextView, tripImageView);

        cardView.setOnClickListener(v -> showContactOptionsDialog(document.getString("contact"), document.getString("email")));

        container.addView(cardView);
    }

    private void populateCardData(DocumentSnapshot document, TextView dateTextView, TextView daysTextView, TextView tripNameTextView, TextView priceTextView, TextView organiserTextView, ImageView tripImageView) {
        Timestamp timestamp = document.getTimestamp("date");
        String no_day = document.getString("no_day");
        String trip_name = document.getString("trip_name");
        String chargers = document.getString("chargers");
        String organiser = document.getString("organiser");
        String tripType = document.getString("type");

        dateTextView.setText(formatDate(timestamp));
        daysTextView.setText(no_day + " Day(s)");
        tripNameTextView.setText(trip_name);
        priceTextView.setText("₹" + chargers + " / Person");
        organiserTextView.setText("Organised by " + organiser);
        setTripImage(tripImageView, tripType);
    }

    private String formatDate(Timestamp timestamp) {
        if (timestamp != null) {
            Date date = timestamp.toDate();
            return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
        }
        return "Date not available";
    }

    private void setTripImage(ImageView imageView, String tripType) {
        if (tripType != null) {
            int imageResource = getImageResource(tripType.toLowerCase());
            imageView.setImageResource(imageResource);
        } else {
            imageView.setImageResource(R.drawable.default_image);
        }
    }

    private int getImageResource(String tripType) {
        switch (tripType) {
            case "trek":
                return R.drawable.trek;
            case "sitescene":
                return R.drawable.site;
            case "prelimimage":
                return R.drawable.prelim;
            default:
                return R.drawable.stranger_explore_img;
        }
    }

    private void showContactOptionsDialog(String phone, String email) {
        new AlertDialog.Builder(this)
                .setTitle("Contact Organizer")
                .setItems(new String[]{"WhatsApp Message", "Email"}, (dialog, which) -> {
                    if (which == 0) {
                        openWhatsApp(phone);
                    } else if (which == 1) {
                        sendEmail(email);
                    }
                })
                .show();
    }

    private void openWhatsApp(String phoneNumber) {
        try {
            phoneNumber = phoneNumber.replaceAll("[^\\d]", "");
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
            intent.setPackage("com.whatsapp");
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            // WhatsApp not installed
            // Handle this scenario (e.g., show a message to the user)
            // You can also try a more generic approach if the above fails:
             Uri uri = Uri.parse("https://api.whatsapp.com/send?phone=" + phoneNumber);
             Intent i = new Intent(Intent.ACTION_VIEW, uri);
             startActivity(i);
        }
    }

    private void sendEmail(String emailAddress) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + emailAddress));
            startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (android.content.ActivityNotFoundException ex) {
            // Handle no email client installed
        }
    }
}