package com.gaganyatris.gaganyatri;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gaganyatris.gaganyatri.utils.LoadingDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class TripPlanActivity extends AppCompatActivity {

    final int statusBarColor = R.color.newStatusBar;
    String tripStartDate, tripEndDate, from, to, budget, tripType;
    boolean exploreNearby;
    int totalTripDays;
    String selectedMode, arrivalDate, returnDate, arrivalTime, returnTime;
    int numberOfTravellers;
    ArrayList<String> coTravellers;
    private static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY; // Replace with your actual API key
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=" + GEMINI_API_KEY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trip_plan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        // Retrieve Intent Extras
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            tripStartDate = bundle.getString("tripStartDate", "Not Set");
            tripEndDate = bundle.getString("tripEndDate", "Not Set");
            from = bundle.getString("from", "Not Set");
            to = bundle.getString("to", "Not Set");
            budget = bundle.getString("budget", "Not Set");
            tripType = bundle.getString("tripType", "Not Set");
            exploreNearby = bundle.getBoolean("exploreNearby", false);
            totalTripDays = bundle.getInt("totalTripDays", 0);
            selectedMode = bundle.getString("selectedMode", "Not Set");
            arrivalDate = bundle.getString("arrivalDate", "Not Set");
            returnDate = bundle.getString("returnDate", "Not Set");
            arrivalTime = bundle.getString("arrivalTime", "Not Set");
            returnTime = bundle.getString("returnTime", "Not Set");
            numberOfTravellers = bundle.getInt("numberOfTravellers", 1);
            coTravellers = bundle.getStringArrayList("coTraveller");
        }

        fetchTripItinerary();
    }

    private void fetchTripItinerary() {
        LoadingDialog progressDialog = new LoadingDialog(this);
        progressDialog.setMessage("Fetching trip itinerary...");
        progressDialog.show();

        new Thread(() -> {
            try {
                URL url = new URL(GEMINI_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Get Intent Data

                // Construct the trip planning prompt
                String prompt = "Plan a trip with the following details:\n\n" +
                        "1. Trip start date: " + tripStartDate + "\n" +
                        "2. Trip end date: " + tripEndDate + "\n" +
                        "3. Current City: " + from + "\n" +
                        "4. Destination City: " + to + "\n" +
                        "5. Trip type: " + tripType + "\n" +
                        "6. Budget: " + budget + "\n" +
                        "7. Number of travellers: " + numberOfTravellers + "\n" +
                        "8. Arrival date at destination city: "+arrivalDate+"\n"+
                        "9. Return date from destination city: " + returnDate + "\n\n" +
                        "10. Subtract Avg trip ticket cost from the budget and then plan the itinerary"+
                        "Provide the itinerary in JSON format: " +
                        "{\"day 1\":{\"date\":\"DD/MM/YYYY\", \"itinerary\":[{\"timeArrival\":\"HH:MM\", \"timeDeparture\":\"HH:MM\", \"loc\":\"Location\"}]}, " +
                        "\"day 2\":{\"date\":\"DD/MM/YYYY\", \"itinerary\":[{\"timeArrival\":\"HH:MM\", \"timeDeparture\":\"HH:MM\", \"loc\":\"Location\"}]}}";

                // Create JSON request body
                JSONObject requestBody = new JSONObject();
                JSONArray contentsArray = new JSONArray();
                JSONObject contentsObject = new JSONObject();
                JSONArray partsArray = new JSONArray();
                JSONObject partsObject = new JSONObject();

                partsObject.put("text", prompt);
                partsArray.put(partsObject);
                contentsObject.put("parts", partsArray);
                contentsArray.put(contentsObject);
                requestBody.put("contents", contentsArray);

                // Send request
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.toString().getBytes());
                    os.flush();
                }

                // Read response
                int responseCode = conn.getResponseCode();
                StringBuilder response = new StringBuilder();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();
                } else {
                    response.append("API Error: ").append(responseCode);
                }

                // Process response
                String result = response.toString();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    TextView i = findViewById(R.id.textView10);
                    i.setText(result);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(TripPlanActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

}