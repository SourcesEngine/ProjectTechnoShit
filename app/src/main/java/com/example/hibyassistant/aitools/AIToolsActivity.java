package com.example.hibyassistant.aitools;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.hibyassistant.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.res.ColorStateList;


public class AIToolsActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 200;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private MaterialButton btnStartDetection;
    private MaterialButton btnVoiceCommand;
    private TextView tvColorResult;
    private TextView tvRecommendation;
    private CircularProgressIndicator progressIndicator;
    private ExecutorService cameraExecutor;
    private boolean isDetecting = false;
    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private View colorIndicator;
    private float lastTouchX = -1;
    private float lastTouchY = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_tools);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("AI Tools");
        }

        // Initialize views
        previewView = findViewById(R.id.cameraPreviewContainer);
        btnStartDetection = findViewById(R.id.btnStartDetection);
        btnVoiceCommand = findViewById(R.id.btnVoiceCommand);
        tvColorResult = findViewById(R.id.tvColorResult);
        tvRecommendation = findViewById(R.id.tvRecommendation);
        progressIndicator = findViewById(R.id.progressIndicator);
        colorIndicator = findViewById(R.id.colorIndicator);

        cameraExecutor = Executors.newSingleThreadExecutor();

        btnStartDetection.setOnClickListener(v -> {
            if (!isDetecting) {
                startColorDetection();
            } else {
                stopColorDetection();
            }
        });

        btnVoiceCommand.setOnClickListener(v -> startVoiceRecognition());

        previewView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastTouchX = event.getX();
                lastTouchY = event.getY();
            }
            return false;
        });
    }

    private void startColorDetection() {
        if (checkPermissions()) {
            isDetecting = true;
            btnStartDetection.setText("Stop Detection");
            startCamera();
        } else {
            requestPermissions();
        }
    }

    private void stopColorDetection() {
        isDetecting = false;
        btnStartDetection.setText("Start Detection");
        tvColorResult.setVisibility(View.GONE);
        tvRecommendation.setVisibility(View.GONE);
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder()
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeImage(ImageProxy image) {
        if (!isDetecting) {
            image.close();
            return;
        }
        try {
            int color;
            if (lastTouchX >= 0 && lastTouchY >= 0) {
                color = getPixelColorAt(image, lastTouchX, lastTouchY, previewView.getWidth(), previewView.getHeight());
                lastTouchX = -1;
                lastTouchY = -1;
            } else {
                color = getCenterPixelColor(image);
            }
            final int finalColor = color;
            runOnUiThread(() -> {
                String colorName = getColorName(finalColor);
                String recommendation = getRecommendation(colorName);
                tvColorResult.setText("Detected Color: " + colorName);
                tvRecommendation.setText(recommendation);
                tvColorResult.setVisibility(View.VISIBLE);
                tvRecommendation.setVisibility(View.VISIBLE);
                colorIndicator.setBackgroundTintList(ColorStateList.valueOf(finalColor));
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            image.close();
        }
    }

    private int getCenterPixelColor(ImageProxy image) {
        // Only YUV_420_888 is supported by CameraX
        if (image.getFormat() != android.graphics.ImageFormat.YUV_420_888) {
            return Color.BLACK;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        ImageProxy.PlaneProxy yPlane = image.getPlanes()[0];
        ByteBuffer yBuffer = yPlane.getBuffer();
        int yRowStride = yPlane.getRowStride();
        int yIndex = centerY * yRowStride + centerX;
        int y = yBuffer.get(yIndex) & 0xFF;
        // For simplicity, treat as grayscale (Y channel only)
        return Color.rgb(y, y, y);
    }

    private String getColorName(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        if (red > 200 && green > 200 && blue > 200) return "White";
        if (red > 240 && green > 230 && blue > 200) return "Cream";
        if (red > 200 && green > 200 && blue < 100) return "Yellow";
        if (red > 150 && green < 100 && blue < 100) return "Brown";
        if (red < 100 && green > 150 && blue < 100) return "Green";
        return "Unknown";
    }

    private String getRecommendation(String colorName) {
        switch (colorName) {
            case "White":
                return "✅ Fresh milk - Safe for baby consumption";
            case "Cream":
                return "✅ Slightly aged milk - Still safe, check expiration date";
            case "Yellow":
                return "⚠️ Spoiling milk - Not recommended for babies";
            case "Brown":
                return "❌ Spoiled milk - DO NOT feed to babies";
            case "Green":
                return "❌ Contaminated milk - Dispose immediately";
            default:
                return "❓ Unable to determine milk quality - Please try again";
        }
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe the milk color");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                String voiceInput = matches.get(0).toLowerCase();
                processVoiceCommand(voiceInput);
            }
        }
    }

    private void processVoiceCommand(String command) {
        String colorName = "Unknown";
        if (command.contains("white")) colorName = "White";
        else if (command.contains("cream")) colorName = "Cream";
        else if (command.contains("yellow")) colorName = "Yellow";
        else if (command.contains("brown")) colorName = "Brown";
        else if (command.contains("green")) colorName = "Green";

        tvColorResult.setText("Voice Detected Color: " + colorName);
        tvRecommendation.setText(getRecommendation(colorName));
        tvColorResult.setVisibility(View.VISIBLE);
        tvRecommendation.setVisibility(View.VISIBLE);
    }

    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startColorDetection();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        cameraExecutor.shutdown();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private int getPixelColorAt(ImageProxy image, float touchX, float touchY, int viewWidth, int viewHeight) {
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();
        int x = (int) (touchX / viewWidth * imgWidth);
        int y = (int) (touchY / viewHeight * imgHeight);
        ImageProxy.PlaneProxy yPlane = image.getPlanes()[0];
        ByteBuffer yBuffer = yPlane.getBuffer();
        int yRowStride = yPlane.getRowStride();
        int yIndex = y * yRowStride + x;
        int yVal = yBuffer.get(yIndex) & 0xFF;
        return Color.rgb(yVal, yVal, yVal);
    }
}