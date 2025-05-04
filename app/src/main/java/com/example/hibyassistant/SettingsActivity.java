package com.example.hibyassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        // Set up click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Profile Settings
        MaterialCardView profileSettingsCard = findViewById(R.id.profileSettingsCard);
        profileSettingsCard.setOnClickListener(v -> {
            // Navigate to profile settings
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // Notification Settings
        MaterialCardView notificationSettingsCard = findViewById(R.id.notificationSettingsCard);
        notificationSettingsCard.setOnClickListener(v -> {
            // TODO: Implement notification settings
            Toast.makeText(this, "Notification settings coming soon", Toast.LENGTH_SHORT).show();
        });

        // Privacy Settings
        MaterialCardView privacySettingsCard = findViewById(R.id.privacySettingsCard);
        privacySettingsCard.setOnClickListener(v -> {
            // TODO: Implement privacy settings
            Toast.makeText(this, "Privacy settings coming soon", Toast.LENGTH_SHORT).show();
        });

        // Sign Out
        MaterialCardView signOutCard = findViewById(R.id.signOutCard);
        signOutCard.setOnClickListener(v -> showSignOutConfirmationDialog());
    }

    private void showSignOutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> signOut())
                .setNegativeButton("No", null)
                .show();
    }

    private void signOut() {
        mAuth.signOut();
        // Redirect to SignInActivity
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