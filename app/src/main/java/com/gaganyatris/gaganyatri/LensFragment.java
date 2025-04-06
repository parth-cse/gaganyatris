package com.gaganyatris.gaganyatri;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class LensFragment extends Fragment {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private File photoFile; // Store the captured photo file

    public LensFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        setupCamera();
                    } else {
                        Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lens, container, false);
        previewView = view.findViewById(R.id.viewFinder);
        ImageButton captureButton = view.findViewById(R.id.captureImage);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            setupCamera();
        }

        captureButton.setOnClickListener(v -> capturePhoto());

        return view;
    }

    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().build();

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void capturePhoto() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String filename = "IMG_" + sdf.format(new Date()) + ".jpg";

        photoFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(requireContext(), "Photo saved: " + photoFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                        sendImageToActivity(Uri.fromFile(photoFile)); // Send the captured image
                    }

                    @Override
                    public void onError(ImageCaptureException exception) {
                        Log.e("CameraX-Debug", "Photo capture failed: " + exception.getMessage(), exception);
                        Toast.makeText(requireContext(), "Photo capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendImageToActivity(Uri imageUri) {
        Intent intent = new Intent(getContext(), AiChatBotActivity.class);
        intent.putExtra("imageUri", imageUri.toString());
        startActivity(intent);
    }
}