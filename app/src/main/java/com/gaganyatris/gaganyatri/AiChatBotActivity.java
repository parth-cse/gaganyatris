package com.gaganyatris.gaganyatri;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.gaganyatris.gaganyatri.utils.LoadingDialog;
import com.gaganyatris.gaganyatri.utils.ZoomImageDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView; // Corrected import

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AiChatBotActivity extends AppCompatActivity {

    private static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=" + GEMINI_API_KEY;

    private ConstraintLayout msgContainer;
    private ConstraintLayout chatContainer;
    private LinearLayout chatBox;
    private EditText etSearch;
    private ImageView sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ai_chat_bot);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        msgContainer = findViewById(R.id.msgContainer);
        chatContainer = findViewById(R.id.chatContainer);
        chatBox = findViewById(R.id.chatBox);
        etSearch = findViewById(R.id.etSearch);
        sendButton = findViewById(R.id.searchIcon);

        String imageUriString = getIntent().getStringExtra("imageUri");

        if (imageUriString != null) {
            handleImageSearch(imageUriString);
        } else {
            handleChatBot();
            addMessageToChat("How may I help you?", false);
        }
    }

    private void handleImageSearch(String imageUriString) {
        msgContainer.setEnabled(false);
        etSearch.setEnabled(false);
        etSearch.setHint("User Input is Disabled when using Gaganyatri Lens");
        sendButton.setEnabled(false);

        LayoutInflater inflater = LayoutInflater.from(this);
        View imageSearchView = inflater.inflate(R.layout.image_search_send, chatBox, false);
        View imageResponseView = inflater.inflate(R.layout.image_search_response, chatBox, false);

        chatBox.addView(imageSearchView);
        chatBox.addView(imageResponseView);

        ShapeableImageView imgPromptRounded = imageSearchView.findViewById(R.id.imgPrompt);

        Uri imageUri = Uri.parse(imageUriString);
        Glide.with(this)
                .load(imageUri)
                .into(imgPromptRounded);

        imgPromptRounded.setOnClickListener(v -> showImageZoom(imageUri));
    }

    private void handleChatBot() {
        sendButton.setOnClickListener(v -> {
            String message = etSearch.getText().toString().trim();
            if (!message.isEmpty()) {
                addMessageToChat(message, true);
                new GeminiApiTask().execute(message);
                etSearch.getText().clear();
            }
        });
    }

    private void addMessageToChat(String message, boolean isUser) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View messageView;
        if (isUser) {
            messageView = inflater.inflate(R.layout.chat_message_user, chatBox, false);
        } else {
            messageView = inflater.inflate(R.layout.chat_message_bot, chatBox, false);
        }
        MaterialTextView messageText = messageView.findViewById(R.id.textView13); // Corrected line
        messageText.setText(message);
        chatBox.addView(messageView);
    }

    private void showImageZoom(Uri imageUri) {
        ZoomImageDialog zoomDialog = new ZoomImageDialog(this, imageUri);
        zoomDialog.show();
    }

    private class GeminiApiTask extends AsyncTask<String, Void, String> {
        private LoadingDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new LoadingDialog(AiChatBotActivity.this);
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... messages) {
            String message = messages[0];
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
                partsObject.put("text", message);
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
                    addMessageToChat(parsedResult, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(AiChatBotActivity.this, "Error parsing API response", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String parseJsonResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray candidates = jsonObject.getJSONArray("candidates");
            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject contentObject = firstCandidate.getJSONObject("content");
            JSONArray parts = contentObject.getJSONArray("parts");
            return parts.getJSONObject(0).getString("text");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}