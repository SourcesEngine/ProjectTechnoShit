package com.example.hibyassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hibyassistant.adapters.PostAdapter;
import com.example.hibyassistant.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private ImageView profileImage;
    private TextView userName;
    private TextView userBio;
    private RecyclerView userPostsRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> userPosts;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get userId from intent
        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
        userBio = findViewById(R.id.userBio);
        userPostsRecyclerView = findViewById(R.id.userPostsRecyclerView);
        toolbar = findViewById(R.id.toolbar);

        // Set up toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");

        // Set up RecyclerView
        userPosts = new ArrayList<>();
        postAdapter = new PostAdapter(userPosts,
            this::onLikeClick,
            this::onCommentClick,
            this::onProfileClick,
            this::onPostLongPress);
        userPostsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userPostsRecyclerView.setAdapter(postAdapter);

        // Load user data
        loadUserData();
        // Load user posts
        loadUserPosts();

        // Set up click listeners
        setupClickListeners();
    }

    private void loadUserData() {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    String bio = documentSnapshot.getString("bio");
                    String imageUrl = documentSnapshot.getString("profileImage");

                    userName.setText(name);
                    if (bio != null && !bio.isEmpty()) {
                        userBio.setText(bio);
                    }

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .circleCrop()
                            .into(profileImage);
                    }
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show());
    }

    private void loadUserPosts() {
        db.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Toast.makeText(this, "Error loading posts", Toast.LENGTH_SHORT).show();
                    return;
                }

                userPosts.clear();
                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {
                        Post post = doc.toObject(Post.class);
                        post.setId(doc.getId());
                        userPosts.add(post);
                    }
                    postAdapter.notifyDataSetChanged();
                }
            });
    }

    private void onLikeClick(Post post) {
        // Implement like functionality
    }

    private void onCommentClick(Post post) {
        // Implement comment functionality
    }

    private void onProfileClick(String profileUserId) {
        // Already in profile view, do nothing
    }

    private void onPostLongPress(Post post, android.view.View view) {
        // Implement post long press functionality
    }

    private void setupClickListeners() {
        // Edit Profile
        ImageView editProfile = findViewById(R.id.editProfile);
        editProfile.setOnClickListener(v -> {
            // TODO: Implement edit profile functionality
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show();
        });

        // Change Password
        findViewById(R.id.changePasswordRow).setOnClickListener(v -> {
            try {
                Intent intent = new Intent(ProfileActivity.this, ForgetPasswordActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // About App
        findViewById(R.id.aboutAppRow).setOnClickListener(v -> {
            // TODO: Implement about app functionality
            Toast.makeText(this, "About App clicked", Toast.LENGTH_SHORT).show();
        });

        // Terms & Conditions
        findViewById(R.id.termsRow).setOnClickListener(v -> {
            // TODO: Implement terms & conditions functionality
            Toast.makeText(this, "Terms & Conditions clicked", Toast.LENGTH_SHORT).show();
        });

        // Privacy Policy
        findViewById(R.id.privacyRow).setOnClickListener(v -> {
            // TODO: Implement privacy policy functionality
            Toast.makeText(this, "Privacy Policy clicked", Toast.LENGTH_SHORT).show();
        });

        // Sign Out
        findViewById(R.id.signOutRow).setOnClickListener(v -> {
            signOut();
        });
    }

    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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