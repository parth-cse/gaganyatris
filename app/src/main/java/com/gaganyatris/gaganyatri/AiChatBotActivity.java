package com.gaganyatris.gaganyatri;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.gaganyatris.gaganyatri.utils.ZoomImageDialog;
import com.google.android.material.imageview.ShapeableImageView;

public class AiChatBotActivity extends AppCompatActivity {

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

        ConstraintLayout msgContainer = findViewById(R.id.msgContainer);
        ConstraintLayout chatContainer = findViewById(R.id.chatContainer);
        LinearLayout chatBox = findViewById(R.id.chatBox);

        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            msgContainer.setEnabled(false);
            EditText etSearch = findViewById(R.id.etSearch);
            etSearch.setEnabled(false);
            etSearch.setHint("User Input is Disabled when using Gaganyatri Lens");

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


    }

    private void showImageZoom(Uri imageUri) {
        // Create a dialog or a new activity to display the zoomed image
        ZoomImageDialog zoomDialog = new ZoomImageDialog(this, imageUri);
        zoomDialog.show();
    }
}