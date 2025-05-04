package com.example.hibyassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class SeeMoreActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_more);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("See More");
        }

        // Set up chat history button click listener
        MaterialCardView chatHistoryButton = findViewById(R.id.chatHistoryButton);
        chatHistoryButton.setOnClickListener(v -> navigateToChatHistory());

        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void navigateToChatHistory() {
        if (mAuth.getCurrentUser() != null) {
            try {
                Intent intent = new Intent(this, ChatHistoryActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Error opening chat history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please log in to view chat history", Toast.LENGTH_SHORT).show();
            try {
                Intent intent = new Intent(this, SignInActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Error opening login screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}