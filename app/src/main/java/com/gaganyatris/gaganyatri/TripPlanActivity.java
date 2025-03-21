package com.gaganyatris.gaganyatri;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TripPlanActivity extends AppCompatActivity {

    final int statusBarColor = R.color.newStatusBar;
    String tripStartDate, tripEndDate, from, to, budget, tripType;
    LinearLayout itinaryContainer;
    boolean exploreNearby;
    int totalTripDays;
    String selectedMode, arrivalDate, returnDate, arrivalTime, returnTime;
    int numberOfTravellers;
    ArrayList<String> coTravellers;
    private static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY; // Replace with your actual API key
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=" + GEMINI_API_KEY;
    TextView tripStartDateTextView, tripEndDateTextView, reachedDestinationTextView, tripDepartureTimeTextView;
    Spinner daySpinner;
    Map<String, JSONObject> dayItineraries = new HashMap<>();

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

        tripStartDateTextView = findViewById(R.id.tripStartDate);
        tripEndDateTextView = findViewById(R.id.tripEndDate);
        reachedDestinationTextView = findViewById(R.id.reachedDestination);
        tripDepartureTimeTextView = findViewById(R.id.tripDepartureTime);
        itinaryContainer = findViewById(R.id.itinaryContainer);
        daySpinner = findViewById(R.id.daySpinner); // Initialize daySpinner



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

        tripStartDateTextView.setText(formatDateTime(tripStartDate, arrivalTime));
        tripEndDateTextView.setText(formatDateTime(tripEndDate, returnTime));
        reachedDestinationTextView.setText(formatDateTime(arrivalDate, arrivalTime));
        tripDepartureTimeTextView.setText(formatDateTime(returnDate, returnTime));


        fetchTripItinerary();
    }

    private String formatDateTime(String date, String time) {
        if ("Not Set".equals(date) || "Not Set".equals(time)) {
            return "Not Set";
        }

        SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); // Changed input format here
        SimpleDateFormat inputTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());

        try {
            Date dateObj = inputDateFormat.parse(date);
            Date timeObj = inputTimeFormat.parse(time);

            // Combine date and time
            SimpleDateFormat combinedFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());// changed combined format here.
            Date combinedDate = combinedFormat.parse(date + " " + time);

            return outputFormat.format(combinedDate);

        } catch (ParseException e) {
            e.printStackTrace();
            return "Error formatting date";
        }
    }


    private void handleItineraryResponse(String jsonResponse) {
        String jsonString = parseJsonResponse(jsonResponse); // Parse the response

        if (jsonString == null) {
            Toast.makeText(this, "Error parsing API response.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject itineraryJson = new JSONObject(jsonString);

            dayItineraries.clear();
            List<String> dayLabels = new ArrayList<>();

            for (int i = 1; i <= itineraryJson.length(); i++) {
                String dayKey = "day " + i;
                if (itineraryJson.has(dayKey)) {
                    JSONObject day = itineraryJson.getJSONObject(dayKey);
                    dayItineraries.put(day.getString("date"), day);
                    dayLabels.add("Day " + i + " - " + day.getString("date"));
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dayLabels);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            daySpinner.setAdapter(adapter);

            daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedDayLabel = (String) parent.getItemAtPosition(position);
                    String selectedDate = selectedDayLabel.substring(selectedDayLabel.lastIndexOf("-") + 2).trim();
                    displayItineraryForDate(selectedDate);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            if (!dayLabels.isEmpty()) {
                displayItineraryForDate(dayItineraries.keySet().iterator().next());
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing itinerary", Toast.LENGTH_SHORT).show();
        }
    }

    // 2. Add parseJsonResponse method:
    private String parseJsonResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray candidates = jsonObject.getJSONArray("candidates");
            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject contentObject = firstCandidate.getJSONObject("content");
            JSONArray parts = contentObject.getJSONArray("parts");
            String textWithMarkdown = parts.getJSONObject(0).getString("text");

            // Extract JSON from markdown
            String jsonString = textWithMarkdown;
            if (textWithMarkdown.startsWith("```json")) {
                jsonString = textWithMarkdown.substring(7, textWithMarkdown.lastIndexOf("```")).trim();
            }

            return jsonString;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 4. Add displayItineraryForDate method:
    private void displayItineraryForDate(String date) {
        itinaryContainer.removeAllViews();
        if (dayItineraries.containsKey(date)) {
            JSONObject day = dayItineraries.get(date);
            try {
                JSONArray itineraryArray = day.getJSONArray("itinerary");
                for (int i = 0; i < itineraryArray.length(); i++) {
                    JSONObject itineraryItem = itineraryArray.getJSONObject(i);
                    View itineraryCard = LayoutInflater.from(this).inflate(R.layout.trip_itinary_planner_layout, itinaryContainer, false);

                    TextView activityName = itineraryCard.findViewById(R.id.activityName);
                    TextView time = itineraryCard.findViewById(R.id.time);
                    TextView location = itineraryCard.findViewById(R.id.textView10);

                    activityName.setText(itineraryItem.optString("activityName", "Activity Name"));
                    time.setText(itineraryItem.optString("timeArrival", "Time"));
                    location.setText(itineraryItem.optString("loc", "Location"));

                    itinaryContainer.addView(itineraryCard);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error displaying itinerary", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // 2. Update fetchTripItinerary method:
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

                // Construct the trip planning prompt
                String prompt = "Plan a trip with the following details:\n\n" +
                        "1. Trip start date: " + tripStartDate + "\n" +
                        "2. Trip end date: " + tripEndDate + "\n" +
                        "3. Current City: " + from + "\n" +
                        "4. Destination City: " + to + "\n" +
                        "5. Trip type: " + tripType + "\n" +
                        "6. Budget: " + budget + "\n" +
                        "7. Number of travellers: " + numberOfTravellers + "\n" +
                        "8. Arrival date at destination city: " + arrivalDate + "\n" +
                        "9. Return date from destination city: " + returnDate + "\n" +
                        "10. Subtract Avg trip ticket cost from the budget and then plan the itinerary\n" +
                        "11. Travel Mode [mode of transport]: "+selectedMode+"\n\n"+
                        "Provide the itinerary in JSON format: " +
                        "{\"day 1\":{\"date\":\"DD/MM/YYYY\", \"itinerary\":[{\"timeArrival\":\"HH:MM\", \"timeDeparture\":\"HH:MM\", \"loc\":\"Location\", \"activityName\":\"Activity Title (Eg. Check IN)\", \"mapView\":\"Location\"}]}, " +
                        "\"day 2\":{\"date\":\"DD/MM/YYYY\", \"itinerary\":[{\"timeArrival\":\"HH:MM\", \"timeDeparture\":\"HH:MM\", \"loc\":\"Location\", \"activityName\":\"Activity Title (Eg. Check IN)\", \"mapView\":\"Location\"}]}}";

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
                    handleItineraryResponse(result); // Call the new method
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