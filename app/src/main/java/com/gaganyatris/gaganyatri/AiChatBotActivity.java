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
    private ConstraintLayout chatContainer;
    private LinearLayout chatBox;
    private EditText etSearch;
    private ImageView sendButton;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ai_chat_bot);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime()); // Get keyboard insets

            // Adjust bottom padding when keyboard is shown
            int bottomPadding = Math.max(systemBars.bottom, imeInsets.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);

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
            addMessageToChat("Hey! Myself Gagoo. How may I help you?", false, true);
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
                addMessageToChat(message, true, false);
                new GeminiApiTask().execute(message);
                etSearch.getText().clear();
            }
        });
    }

    private void addMessageToChat(String message, boolean isUser, boolean typingAnimation) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View messageView;
        if (isUser) {
            messageView = inflater.inflate(R.layout.chat_message_user, chatBox, false);
        } else {
            messageView = inflater.inflate(R.layout.chat_message_bot, chatBox, false);
        }
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
        ZoomImageDialog zoomDialog = new ZoomImageDialog(this, imageUri);
        zoomDialog.show();
    }

    private class GeminiApiTask extends AsyncTask<String, Void, String> {
        private LoadingDialog progressDialog;
        private String userMessage;

        private TextView botMessageText;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Add a temporary bot message with the "thinking" animation
            LayoutInflater inflater = LayoutInflater.from(AiChatBotActivity.this);
            View botMessageView = inflater.inflate(R.layout.chat_message_bot, chatBox, false);
            botMessageText = botMessageView.findViewById(R.id.textView13);
            chatBox.addView(botMessageView);
            animateThinkingDots(botMessageText);
        }

        private void animateThinkingDots(final TextView textView) {
            final String[] dots = {".", "..", "..."};
            final int[] index = {0};
            final Handler handler = new Handler();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    textView.setText(dots[index[0]]);
                    index[0] = (index[0] + 1) % 3; // Cycle through dots
                    handler.postDelayed(this, 500); // Adjust delay for speed
                }
            };
            handler.post(runnable);
            textView.setTag(runnable); // Store runnable to cancel later
        }

        private void stopThinkingAnimation(TextView textView) {
            Handler handler = new Handler();
            Runnable runnable = (Runnable) textView.getTag();
            if (runnable != null) {
                handler.removeCallbacks(runnable);
            }
            botMessageText.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... messages) {
            userMessage = messages[0];
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
                partsObject.put("text", "Act as a Travel Guide named 'Nimbus' and answer the User Query: " +userMessage+" when If User asked you about yur name reply it as Gagoo and please keep your response consive to 300 characters. Reply in paragraph form only.");
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
            stopThinkingAnimation(botMessageText);
            try {
                String parsedResult = parseJsonResponse(result, userMessage);
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
    }

    private String parseJsonResponse(String jsonResponse, String userMessage) {
        try {
//            if (userMessage.toLowerCase().contains("what's your name") || userMessage.toLowerCase().contains("what is your name") || userMessage.toLowerCase().contains("your name")) {
//                return "My name is Gagoo!";
//            }
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray candidates = jsonObject.getJSONArray("candidates");
            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject contentObject = firstCandidate.getJSONObject("content");
            JSONArray parts = contentObject.getJSONArray("parts");
            return parts.getJSONObject(0).getString("text").trim();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}