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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ExploreLocationResultActivity extends AppCompatActivity {

    private static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;
    private LinearLayout activityContainer;
    private String from, time, tripType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_explore_location_result);

        setupWindowInsets();
        setupUI();
        getIntentData();
        fetchLocationSuggestions();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.newStatusBar));
    }

    private void setupUI() {
        ImageButton backBTN = findViewById(R.id.backBtn);
        backBTN.setOnClickListener(v -> finish());
        activityContainer = findViewById(R.id.activityContainer);
    }

    private void getIntentData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            from = extras.getString("from");
            time = extras.getString("time");
            tripType = extras.getString("tripType");
        }
    }

    private void fetchLocationSuggestions() {
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
                return fetchApiResponse();
            } catch (IOException | JSONException e) {
                Log.e("API_ERROR", "Error fetching API response", e);
                return "Error: " + e.getMessage();
            }
        }

        private String fetchApiResponse() throws IOException, JSONException {
            URL url = new URL(GEMINI_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject requestBody = createRequestBody();

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toString().getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readResponse(conn);
            } else {
                return "API Error: " + responseCode;
            }
        }

        private JSONObject createRequestBody() throws JSONException {
            JSONObject requestBody = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject contentsObject = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject partsObject = new JSONObject();

            partsObject.put("text", buildGeminiPrompt());
            partsArray.put(partsObject);
            contentsObject.put("parts", partsArray);
            contentsArray.put(contentsObject);
            requestBody.put("contents", contentsArray);
            return requestBody;
        }

        private String readResponse(HttpURLConnection conn) throws IOException {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (result != null && !result.startsWith("Error")) {
                try {
                    updateUI(parseJsonResponse(result));
                } catch (JSONException e) {
                    Log.e("JSON_ERROR", "Error parsing JSON response", e);
                    Toast.makeText(ExploreLocationResultActivity.this, "Error parsing API response", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(ExploreLocationResultActivity.this, result, Toast.LENGTH_LONG).show();
            }
        }
    }

    private String buildGeminiPrompt() {
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().getTime());
        String prompt = "I am getting bored and have " + time + " to explore. I want to explore of following Type: " + tripType;

        if (tripType.equalsIgnoreCase("Have some food") || tripType.equalsIgnoreCase("explore")) {
            prompt += " If the tripType is Have some food or explore based on current time stamp " + currentTime;
            prompt += " Like if its lunch time, snacks time or dinner Time, ";
        }

        prompt += " suggest me places that can be explored in the given Time based according to my current location " + from;
        prompt += " give output in JSON {0: {\"name\":\"name of location\", \"Activity Name\": \"Name of Activity\", \"Map Location\" : \"Google maps intent Location direction link\", \"time required\": \"Time required to explore including average Travel Time\", \"available mode\":\"mode of transport to get to that Location\", \"Location Description\":\"Location Details in short Paragraph\"}}";
        prompt += " Don't Recommend Activities if average travel time required to and fro adding the time required to expore the location is more than the Time " + time + " available.";
        return prompt;
    }

    private String parseJsonResponse(String jsonResponse) throws JSONException {
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
    }

    private void updateUI(String jsonString) throws JSONException {
        JSONObject locationSuggestions = new JSONObject(jsonString);
        for (int i = 0; locationSuggestions.has(String.valueOf(i)); i++) {
            JSONObject location = locationSuggestions.getJSONObject(String.valueOf(i));
            addLocationCard(location);
        }
    }

    private void addLocationCard(JSONObject location) throws JSONException {
        View cardView = LayoutInflater.from(this).inflate(R.layout.explore_your_location_card, activityContainer, false);
        TextView activityName = cardView.findViewById(R.id.activityName);
        TextView timeTextView = cardView.findViewById(R.id.time);
        TextView locationTextView = cardView.findViewById(R.id.textView10);
        TextView modeTextView = cardView.findViewById(R.id.textView20);
        TextView descriptionTextView = cardView.findViewById(R.id.locDes);
        TextView viewOnMap = cardView.findViewById(R.id.viewOnMap);

        activityName.setText(location.getString("name"));
        timeTextView.setText(location.getString("time required"));
        locationTextView.setText(location.getString("Activity Name"));
        modeTextView.setText("Available Mode of Transport: " + location.getString("available mode"));
        descriptionTextView.setText("Location Description: " + location.getString("Location Description"));

        setupMapViewIntent(viewOnMap, locationTextView, location.getString("Map Location"));
        activityContainer.addView(cardView);
    }

    private void setupMapViewIntent(TextView viewOnMap, TextView locationTextView, String mapLink) {
        viewOnMap.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mapLink)));
            } catch (Exception e) {
                Log.e("MAP", "Error opening map link", e);
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(locationTextView.getText().toString()))));
                } catch (Exception geoError) {
                    Log.e("MAP", "Error opening geo intent", geoError);
                    Toast.makeText(ExploreLocationResultActivity.this, "Invalid map link.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}