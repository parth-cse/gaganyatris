package com.gaganyatris.gaganyatri;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class UploadImageFragment extends Fragment {
    Button uploadBTN;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public UploadImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        uploadImage();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_image, container, false);

        uploadBTN = view.findViewById(R.id.uploadBtn);

        uploadBTN.setOnClickListener(v -> openImagePicker());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // You can also add openImagePicker() here if you want it to trigger on resume.
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // Add this line to make the intent chooser look like the screenshot
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/jpeg", "image/png", "image/jpg"});
        imagePickerLauncher.launch(intent);
    }

    private void sendImageToActivity(Uri imageUri) {
        Intent intent = new Intent(getContext(), AiChatBotActivity.class);
        intent.putExtra("imageUri", imageUri.toString()); // Pass the Uri as a String
        startActivity(intent);
    }

    private void uploadImage() {
        if (selectedImageUri != null) {
            sendImageToActivity(selectedImageUri);
        } else {
            Toast.makeText(getContext(), "Please select an image.", Toast.LENGTH_SHORT).show();
        }
    }
}