package com.example.hibyassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.example.hibyassistant.utils.NetworkUtils;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Enable offline persistence
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db.setFirestoreSettings(settings);
        // In your Application class or MainActivity

        // Initialize views
        tvUserName = findViewById(R.id.tvUserName);
        MaterialCardView communityForumButton = findViewById(R.id.communityForumButton);

        // Check if user is signed in
        if (mAuth.getCurrentUser() == null) {
            // User is not signed in, redirect to SignInActivity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        // Load user data
        loadUserData();

        // Initialize bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Handle home navigation
                return true;
            } else if (itemId == R.id.nav_calendar) {
                // Handle calendar navigation
                return true;
            } else if (itemId == R.id.nav_chat) {
                // Navigate to ChatBotActivity
                startActivity(new Intent(MainActivity.this, ChatBotActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Navigate to SettingsActivity
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        // Set default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // Set up feature button click listeners
        setupFeatureButtons();

        // Set click listener for Community Forum button
        communityForumButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CommunityForumActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check internet connectivity when activity resumes
        if (!isNetworkAvailable()) {
            showMessage("You're offline - Some features may be limited");
            // Don't sign out, just show offline message
        }
    }

    private void setupFeatureButtons() {
        // Tracking button
        findViewById(R.id.trackingButton).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TrackingActivity.class));
        });

        // Health Alert button
        findViewById(R.id.healthAlertButton).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HealthAlertActivity.class));
        });

        // Milestone button
        findViewById(R.id.milestoneButton).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MilestoneActivity.class));
        });

        // Support button
        findViewById(R.id.supportButton).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SupportActivity.class));
        });

        // AI Tools button
        findViewById(R.id.aiToolsButton).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AIToolsActivity.class));
        });

        // See More button
        findViewById(R.id.seeMoreButton).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SeeMoreActivity.class));
        });

        // Chat History button
        findViewById(R.id.chatHistoryButton).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ChatHistoryActivity.class));
        });

        // Hi-By Tips button
        findViewById(R.id.hiByTipsButton).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HiByTipsActivity.class));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sign_out) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        mAuth.signOut();
        // Redirect to SignInActivity
        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        
        // First try to get cached data
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvUserName.setText(name);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isNetworkAvailable()) {
                        // If offline, try to get cached data
                        db.collection("users").document(userId)
                                .get(com.google.firebase.firestore.Source.CACHE)
                                .addOnSuccessListener(cachedSnapshot -> {
                                    if (cachedSnapshot.exists()) {
                                        String name = cachedSnapshot.getString("name");
                                        if (name != null && !name.isEmpty()) {
                                            tvUserName.setText(name);
                                            showMessage("Showing cached data - You're offline");
                                        }
                                    } else {
                                        showMessage("No cached data available - Please check your connection");
                                    }
                                })
                                .addOnFailureListener(cacheError -> {
                                    showMessage("Unable to load data - Please check your connection");
                                });
                    } else {
                        showMessage("Error loading user data: " + e.getMessage());
                    }
                });

        // If online, update the cache in the background
        if (isNetworkAvailable()) {
            db.collection("users").document(userId)
                    .get(com.google.firebase.firestore.Source.SERVER)
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name != null && !name.isEmpty()) {
                                tvUserName.setText(name);
                            }
                        }
                    });
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean isNetworkAvailable() {
        return NetworkUtils.isNetworkAvailable(this);
    }

    private void handleOfflineState() {
        // Show offline message but don't sign out
        showMessage("You're offline - Some features may be limited");
        
        // Try to load cached data
        if (mAuth.getCurrentUser() != null) {
            loadUserData();
        }
    }
}