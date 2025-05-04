package com.example.hibyassistant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hibyassistant.models.VideoItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HiByTipsActivity extends AppCompatActivity {
    private static final int PICK_VIDEO_REQUEST = 1;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddVideo;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private ActivityResultLauncher<Intent> videoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hi_by_tips);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        fabAddVideo = findViewById(R.id.fabAddVideo);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Hi-By Tips");
        }

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize video picker launcher
        videoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri videoUri = result.getData().getData();
                    uploadVideo(videoUri);
                }
            }
        );

        // Set up FAB click listener
        fabAddVideo.setOnClickListener(v -> openVideoPicker());

        // Load videos
        loadVideos();
    }

    private void openVideoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        videoPickerLauncher.launch(intent);
    }

    private void uploadVideo(Uri videoUri) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to upload videos", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();
        String videoId = db.collection("videos").document().getId();
        StorageReference videoRef = storage.getReference()
            .child("videos")
            .child(userId)
            .child(videoId);

        videoRef.putFile(videoUri)
            .addOnSuccessListener(taskSnapshot -> {
                videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Map<String, Object> videoData = new HashMap<>();
                    videoData.put("userId", userId);
                    videoData.put("videoUrl", uri.toString());
                    videoData.put("timestamp", System.currentTimeMillis());
                    videoData.put("title", "Parenting Tip Video");
                    videoData.put("description", "A helpful parenting tip video");

                    db.collection("videos")
                        .document(videoId)
                        .set(videoData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Video uploaded successfully", Toast.LENGTH_SHORT).show();
                            loadVideos();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error saving video data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        });
                });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error uploading video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            });
    }

    private void loadVideos() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("videos")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<VideoItem> videoList = new ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    VideoItem video = document.toObject(VideoItem.class);
                    video.setId(document.getId());
                    videoList.add(video);
                }
                // TODO: Set up adapter and display videos
                progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading videos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 