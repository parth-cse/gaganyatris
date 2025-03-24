package com.gaganyatris.gaganyatri;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ExploreLocationResultActivity extends AppCompatActivity {

    final int statusBarColor = R.color.newStatusBar;
    String from, time, tripType;
    private static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=" + GEMINI_API_KEY;
    private LinearLayout activityContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_explore_location_result);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));
        ImageButton backBTN = findViewById(R.id.backBtn);
        backBTN.setOnClickListener(v -> finish());
        activityContainer = findViewById(R.id.activityContainer);

        // Access data from the Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            from = extras.getString("from");
            time = extras.getString("time");
            tripType = extras.getString("tripType");
        }

        new FetchLocationSuggestionsTask().execute();
    }

    private class FetchLocationSuggestionsTask extends AsyncTask<Void, Void, String> {
        private LoadingDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new LoadingDialog(ExploreLocationResultActivity.this);
            progressDialog.setMessage("Fetching location suggestions...");
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

                String prompt = buildGeminiPrompt();

                partsObject.put("text", prompt);
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
                Toast.makeText(ExploreLocationResultActivity.this, "Error parsing API response", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String buildGeminiPrompt() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(calendar.getTime());

        String prompt = "I am getting bored and have " + time + " to explore. I want to explore of following Type: " + tripType;
        if (tripType.equalsIgnoreCase("Have some food") || tripType.equalsIgnoreCase("explore")) {
            prompt += " If the tripType is Have some food or explore based on current time stamp " + currentTime;
            prompt += " Like if its lunch time, snacks time or dinner Time, ";
        }
        prompt += " suggest me places that can be explored in the given Time based according to my current location " + from;
        prompt += " give output in JSON {0: {\"name\":\"name of location\", \"Activity Name\": \"Name of Activity\", \"Map Location\" : \"Google maps intent Link\", \"time required\": \"Time required to explore including average Travel Time\", \"available mode\":\"mode of transport to get to that Location\", \"Location Description\":\"Location Details in short Paragraph\"}}";
        prompt += " Don't Recommend Activities if average travel time required to and fro adding the time required to expore the location is more than the Time " + time + " available.";
        return prompt;
    }

    private String parseJsonResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray candidates = jsonObject.getJSONArray("candidates");
            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject contentObject = firstCandidate.getJSONObject("content");
            JSONArray parts = contentObject.getJSONArray("parts");
            String textWithMarkdown = parts.getJSONObject(0).getString("text");

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

    private void updateUI(String jsonString) {
        try {
            JSONObject locationSuggestions = new JSONObject(jsonString);
            for (int i = 0; locationSuggestions.has(String.valueOf(i)); i++) {
                JSONObject location = locationSuggestions.getJSONObject(String.valueOf(i));
                View cardView = LayoutInflater.from(this).inflate(R.layout.explore_your_location_card, activityContainer, false);

                TextView activityName = cardView.findViewById(R.id.activityName);
                TextView timeTextView = cardView.findViewById(R.id.time);
                TextView locationTextView = cardView.findViewById(R.id.textView10);
                TextView modeTextView = cardView.findViewById(R.id.textView20);
                TextView descriptionTextView = cardView.findViewById(R.id.locDes);
                TextView viewOnMap = cardView.findViewById(R.id.viewOnMap);

                String Mode = "Available Mode of Transport: " + location.getString("available mode");
                String mapLink = location.getString("Map Location");

                activityName.setText(location.getString("Activity Name"));
                timeTextView.setText(location.getString("time required"));
                locationTextView.setText(location.getString("name"));
                modeTextView.setText(Mode);
                descriptionTextView.setText("Location Description: " + location.getString("Location Description"));

                viewOnMap.setOnClickListener(v -> {
                    try {
                        Uri mapUri = Uri.parse(mapLink);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
                        startActivity(mapIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("MAP",""+e);
                        try{
                            Uri geoUri = Uri.parse("geo:0,0?q=" + Uri.encode(locationTextView.getText().toString()));
                            Intent geoIntent = new Intent(Intent.ACTION_VIEW, geoUri);
                            if (geoIntent.resolveActivity(getPackageManager()) != null) {
                                startActivity(geoIntent);
                            } else {
                                Toast.makeText(ExploreLocationResultActivity.this, "Cannot open map link.", Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception geoError){
                            geoError.printStackTrace();
                            Toast.makeText(ExploreLocationResultActivity.this, "Invalid map link.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                activityContainer.addView(cardView);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ExploreLocationResultActivity.this, "Error updating UI", Toast.LENGTH_LONG).show();
        }
    }
}