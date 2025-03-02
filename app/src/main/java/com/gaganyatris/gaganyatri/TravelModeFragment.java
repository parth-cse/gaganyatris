package com.gaganyatris.gaganyatri;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gaganyatris.gaganyatri.utils.LoadingDialog;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TravelModeFragment extends Fragment {

    private static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY; // Replace with your actual API key
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=" + GEMINI_API_KEY;

    private String startDateString, endDateString, from, to, budgetAmount, tripType;
    private boolean exploreNearby;
    private int totalTripDays = 0;
    private String selectedMode = ""; // Variable to store selected travel mode
    ConstraintLayout flightOption, trainOption, busOption, cabOption;
    Button btnSave;
    TextView flightTimeView, trainTimeView, busTimeView, cabTimeView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_travel_mode, container, false);

        // Initialize UI elements
        LinearLayout btnBack = view.findViewById(R.id.btn_back);
        btnSave = view.findViewById(R.id.btn_save);
        flightOption = view.findViewById(R.id.op_flight);
        trainOption = view.findViewById(R.id.op_train);
        busOption = view.findViewById(R.id.op_bus);
        cabOption = view.findViewById(R.id.op_others);
        flightTimeView = view.findViewById(R.id.flight_time);
        trainTimeView = view.findViewById(R.id.train_time);
        busTimeView = view.findViewById(R.id.bus_time);
        cabTimeView = view.findViewById(R.id.by_road_time);

        // Handle Back Button Click
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                FragmentManager fm = requireActivity().getSupportFragmentManager();
                fm.popBackStack();
            });
        }

        // Retrieve data from Bundle
        if (getArguments() != null) {
            startDateString = getArguments().getString("tripStartDate", "");
            endDateString = getArguments().getString("tripEndDate", "");
            from = getArguments().getString("from", "");
            to = getArguments().getString("to", "");
            budgetAmount = getArguments().getString("budget", "");
            tripType = getArguments().getString("tripType", "");
            exploreNearby = getArguments().getBoolean("exploreNearby", false);
        }

        // Calculate total trip days
        calculateTripDays();

        // Fetch travel times
        new FetchTravelTimesTask().execute();

        // Set click listeners for travel mode selection
        flightOption.setOnClickListener(v -> selectTravelMode("Flight", flightOption));
        trainOption.setOnClickListener(v -> selectTravelMode("Train", trainOption));
        busOption.setOnClickListener(v -> selectTravelMode("Bus", busOption));
        cabOption.setOnClickListener(v -> selectTravelMode("Cab", cabOption));

        // Handle Save Button Click
        btnSave.setOnClickListener(v -> {
            if (selectedMode.isEmpty()) {
                Toast.makeText(getContext(), "Please select a travel mode", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Selected Mode: " + selectedMode, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
    // Method to calculate total trip days
    private void calculateTripDays() {
        if (!startDateString.isEmpty() && !endDateString.isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date startDate = dateFormat.parse(startDateString);
                Date endDate = dateFormat.parse(endDateString);

                if (startDate != null && endDate != null) {
                    long diffMillis = endDate.getTime() - startDate.getTime();
                    totalTripDays = (int) (diffMillis / (1000 * 60 * 60 * 24)) + 1; // Include start date
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to handle travel mode selection
    private void selectTravelMode(String mode, ConstraintLayout selected) {
        selectedMode = mode;
        resetOptions();
        selected.setBackgroundResource(R.drawable.card_language_active);
    }

    private void resetOptions() {
        flightOption.setBackgroundResource(R.drawable.card_settings);
        trainOption.setBackgroundResource(R.drawable.card_settings);
        busOption.setBackgroundResource(R.drawable.card_settings);
        cabOption.setBackgroundResource(R.drawable.card_settings);
    }

    // AsyncTask to call Gemini API using HTTP with ProgressDialog
    // AsyncTask to call Gemini API using HTTP with ProgressDialog
    private class FetchTravelTimesTask extends AsyncTask<Void, Void, String> {
        private LoadingDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new LoadingDialog(requireContext());
            progressDialog.setMessage("Fetching travel times...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(GEMINI_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject requestBody = new JSONObject();
                JSONArray contentsArray = new JSONArray();
                JSONObject contentsObject = new JSONObject();
                JSONArray partsArray = new JSONArray();
                JSONObject partsObject = new JSONObject();
                partsObject.put("text", "Provide the average travel time for Flight, Train, Bus, and Cab from " + from + " to " + to + ". Provide the result in JSON format: {\"flight\":{\"available\":\"true\", \"avg_time\":\"X hr\"}, \"train\":{\"available\":\"true\", \"avg_time\":\"X hr\"}, \"bus\":{\"available\":\"true\", \"avg_time\":\"X hr\"}, \"cab\":{\"available\":\"true\", \"avg_time\":\"X hr\"}}");
                partsArray.put(partsObject);
                contentsObject.put("parts", partsArray);
                contentsArray.put(contentsObject);
                requestBody.put("contents", contentsArray);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.toString().getBytes());
                    os.flush();
                }

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();
                    return response.toString();
                } else {
                    return "API Error: " + responseCode;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            try {
                String parsedResult = parseJsonResponse(result);
                if (parsedResult != null) {
                    updateUI(parsedResult);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error parsing API response", Toast.LENGTH_LONG).show();
            }
        }
    }
    // Parse JSON response from Gemini AI
    // Parse JSON response from Gemini AI
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
    // Update UI with travel times
    private void updateUI(String jsonString) {
        try {
            JSONObject travelTimes = new JSONObject(jsonString);

            updateTravelOption("flight", travelTimes.getJSONObject("flight"), flightTimeView, flightOption);
            updateTravelOption("train", travelTimes.getJSONObject("train"), trainTimeView, trainOption);
            updateTravelOption("bus", travelTimes.getJSONObject("bus"), busTimeView, busOption);
            updateTravelOption("cab", travelTimes.getJSONObject("cab"), cabTimeView, cabOption);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error updating UI", Toast.LENGTH_LONG).show();
        }
    }

    private void updateTravelOption(String mode, JSONObject modeData, TextView timeView, ConstraintLayout optionLayout) {
        try {
            boolean available = modeData.getString("available").equalsIgnoreCase("true");
            String avgTime = modeData.getString("avg_time");

            if (available) {
                timeView.setText("Average Time: " + avgTime);
                if(totalTripDays > 0) {
                    double timeInHours = parseTime(avgTime);
                    double totalHours = totalTripDays * 24.0;
                    if (timeInHours > totalHours / 3.0) {
                        optionLayout.setEnabled(false);
                        optionLayout.setAlpha(0.5f);
                    }else{
                        optionLayout.setEnabled(true);
                        optionLayout.setAlpha(1f);
                    }
                }

            } else {
                timeView.setText("Not Available");
                optionLayout.setEnabled(false);
                optionLayout.setAlpha(0.5f);
            }
        } catch (Exception e) {
            e.printStackTrace();
            timeView.setText("Error");
            optionLayout.setEnabled(false);
            optionLayout.setAlpha(0.5f);
        }
    }

    private double parseTime(String timeString) {
        try {
            String[] parts = timeString.split(" ");
            double time = Double.parseDouble(parts[0]);
            if (parts.length > 1 && parts[1].equalsIgnoreCase("min")) {
                time /= 60.0; // Convert minutes to hours
            }
            return time;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

}
