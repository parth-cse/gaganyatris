package com.gaganyatris.gaganyatri;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AiChatBotActivity extends AppCompatActivity {

    private static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;

    private ConstraintLayout msgContainer;
    private EditText etSearch;
    private ImageView sendButton;
    private LinearLayout chatBox;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ai_chat_bot);
        setupWindowInsets();
        initializeViews();
        handleIntentData();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomPadding = Math.max(systemBars.bottom, imeInsets.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);
            return insets;
        });
    }

    private void initializeViews() {
        msgContainer = findViewById(R.id.msgContainer);
        etSearch = findViewById(R.id.etSearch);
        sendButton = findViewById(R.id.searchIcon);
        chatBox = findViewById(R.id.chatBox);
    }

    private void handleIntentData() {
        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            handleImageSearch(imageUriString);
        } else {
            handleChatBot();
            addMessageToChat("Hey! Myself Nimbus. How may I help you?", false, true);
        }
    }

    private void handleImageSearch(String imageUriString) {
        disableUserInput("User Input is Disabled when using Gaganyatri Lens");
        displayImageSearchUi(imageUriString);
    }

    private void disableUserInput(String hint) {
        msgContainer.setEnabled(false);
        etSearch.setEnabled(false);
        etSearch.setHint(hint);
        sendButton.setEnabled(false);
    }

    private void displayImageSearchUi(String imageUriString) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View imageSearchView = inflater.inflate(R.layout.image_search_send, chatBox, false);
        View imageResponseView = inflater.inflate(R.layout.image_search_response, chatBox, false);
        chatBox.addView(imageSearchView);
        chatBox.addView(imageResponseView);

        ShapeableImageView imgPromptRounded = imageSearchView.findViewById(R.id.imgPrompt);
        Uri imageUri = Uri.parse(imageUriString);
        Glide.with(this).load(imageUri).into(imgPromptRounded);
        imgPromptRounded.setOnClickListener(v -> showImageZoom(imageUri));
    }

    private void handleChatBot() {
        sendButton.setOnClickListener(v -> {
            String message = etSearch.getText().toString().trim();
            if (!message.isEmpty()) {
                addMessageToChat(message, true, false);
                new GeminiApiTask().execute(message);
                etSearch.getText().clear();
            }
        });
    }

    private void addMessageToChat(String message, boolean isUser, boolean typingAnimation) {
        View messageView = LayoutInflater.from(this).inflate(isUser ? R.layout.chat_message_user : R.layout.chat_message_bot, chatBox, false);
        MaterialTextView messageText = messageView.findViewById(R.id.textView13);
        chatBox.addView(messageView);

        if (typingAnimation) {
            animateTextTyping(messageText, message);
        } else {
            messageText.setText(message);
        }
    }

    private void animateTextTyping(MaterialTextView textView, String message) {
        final int[] index = {0};
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (index[0] < message.length()) {
                    textView.setText(message.substring(0, index[0] + 1));
                    index[0]++;
                    handler.postDelayed(this, 50);
                }
            }
        });
    }

    private void showImageZoom(Uri imageUri) {
        new ZoomImageDialog(this, imageUri).show();
    }

    private class GeminiApiTask extends AsyncTask<String, Void, String> {
        private TextView botMessageText;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            botMessageText = addBotTypingView();
        }

        private TextView addBotTypingView() {
            View botMessageView = LayoutInflater.from(AiChatBotActivity.this).inflate(R.layout.chat_message_bot, chatBox, false);
            TextView textView = botMessageView.findViewById(R.id.textView13);
            chatBox.addView(botMessageView);
            animateThinkingDots(textView);
            return textView;
        }

        private void animateThinkingDots(final TextView textView) {
            final String[] dots = {".", "..", "..."};
            final int[] index = {0};
            final Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    textView.setText(dots[index[0]]);
                    index[0] = (index[0] + 1) % 3;
                    handler.postDelayed(this, 500);
                }
            };
            handler.post(runnable);
            textView.setTag(runnable);
        }

        private void stopThinkingAnimation() {
            if (botMessageText != null && botMessageText.getTag() instanceof Runnable) {
                handler.removeCallbacks((Runnable) botMessageText.getTag());
                botMessageText.setVisibility(View.GONE);
            }
        }

        @Override
        protected String doInBackground(String... messages) {
            return fetchApiResponse(messages[0]);
        }

        private String fetchApiResponse(String userMessage) {
            try {
                URL url = new URL(GEMINI_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject requestBody = createRequestBody(userMessage);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.toString().getBytes());
                    os.flush();
                }

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return readResponse(conn);
                } else {
                    return "API Error: " + conn.getResponseCode();
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        private JSONObject createRequestBody(String userMessage) throws org.json.JSONException {
            JSONObject requestBody = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject contentsObject = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject partsObject = new JSONObject();
            partsObject.put("text", "Act as a Travel Guide named 'Nimbus' and answer the User Query: " + userMessage + " when If User asked you about yur name reply it as Nimbus and please keep your response consive to 300 characters. Reply in paragraph form only.");
            partsArray.put(partsObject);
            contentsObject.put("parts", partsArray);
            contentsArray.put(contentsObject);
            requestBody.put("contents", contentsArray);
            return requestBody;
        }

        private String readResponse(HttpURLConnection conn) throws java.io.IOException {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            return response.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            stopThinkingAnimation();
            displayApiResponse(result);
        }

        private void displayApiResponse(String result) {
            try {
                String parsedResult = parseApiResponse(result);
                if (parsedResult != null) {
                    addMessageToChat(parsedResult, false, true);
                } else {
                    addMessageToChat("Sorry, I couldn't get a response.", false, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                addMessageToChat("Sorry, there was an error.", false, true);
            }
        }

        private String parseApiResponse(String jsonResponse) throws org.json.JSONException {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray candidates = jsonObject.getJSONArray("candidates");
            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject contentObject = firstCandidate.getJSONObject("content");
            JSONArray parts = contentObject.getJSONArray("parts");
            return parts.getJSONObject(0).getString("text").trim();
        }
    }
}