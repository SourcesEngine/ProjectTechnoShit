package com.example.hibyassistant;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hibyassistant.adapters.MilestoneAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MilestoneActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView recommendationsText;
    private String userBirthdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_milestone);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        FloatingActionButton fabAddMilestone = findViewById(R.id.fabAddMilestone);
        recommendationsText = findViewById(R.id.recommendationsText);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Milestones");
        }

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up FAB click listener
        fabAddMilestone.setOnClickListener(v -> showAddMilestoneDialog());

        // Fetch birthdate and show recommendations
        fetchOrPromptBirthdate();

        // Load milestones
        loadMilestones();
    }

    private void showAddMilestoneDialog() {
        try {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            View view = getLayoutInflater().inflate(R.layout.dialog_add_milestone, null);
            builder.setView(view);

            // Fields
            AutoCompleteTextView milestoneTypeInput = view.findViewById(R.id.milestoneTypeInput);
            com.google.android.material.textfield.TextInputEditText descriptionInput = view.findViewById(R.id.descriptionInput);
            com.google.android.material.textfield.TextInputEditText weightInput = view.findViewById(R.id.weightInput);
            com.google.android.material.textfield.TextInputEditText heightInput = view.findViewById(R.id.heightInput);
            com.google.android.material.textfield.TextInputEditText percentileInput = view.findViewById(R.id.percentileInput);
            com.google.android.material.switchmaterial.SwitchMaterial createAlertSwitch = view.findViewById(R.id.createAlertSwitch);

            // Set up milestone type dropdown
            String[] milestoneTypes = {"Growth", "Physical", "Cognitive", "Social", "Feeding", "Sleep", "Other"};
            ArrayAdapter<String> milestoneTypeAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, milestoneTypes);
            milestoneTypeInput.setAdapter(milestoneTypeAdapter);
            milestoneTypeInput.setText("", false); // Clear initial text

            androidx.appcompat.app.AlertDialog dialog = builder
                .setTitle("Add Milestone")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .create();

            dialog.setOnShowListener(dialogInterface -> {
                androidx.appcompat.app.AlertDialog alertDialog = (androidx.appcompat.app.AlertDialog) dialogInterface;
                android.widget.Button button = alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view1 -> {
                    try {
                        String milestoneType = milestoneTypeInput.getText() != null ? milestoneTypeInput.getText().toString().trim() : "";
                        String description = descriptionInput.getText() != null ? descriptionInput.getText().toString().trim() : "";
                        String weight = weightInput.getText() != null ? weightInput.getText().toString().trim() : "";
                        String height = heightInput.getText() != null ? heightInput.getText().toString().trim() : "";
                        String percentile = percentileInput.getText() != null ? percentileInput.getText().toString().trim() : "";
                        boolean createAlert = createAlertSwitch.isChecked();

                        // Validate required fields
                        if (milestoneType.isEmpty()) {
                            milestoneTypeInput.setError("Please select a milestone type");
                            return;
                        }

                        if (description.isEmpty()) {
                            descriptionInput.setError("Please enter a description");
                            return;
                        }

                        // Validate numeric fields if they are not empty
                        if (!weight.isEmpty() && !isValidNumber(weight)) {
                            weightInput.setError("Please enter a valid number");
                            return;
                        }

                        if (!height.isEmpty() && !isValidNumber(height)) {
                            heightInput.setError("Please enter a valid number");
                            return;
                        }

                        if (!percentile.isEmpty() && !isValidNumber(percentile)) {
                            percentileInput.setError("Please enter a valid number");
                            return;
                        }

                        // Clear any previous errors
                        milestoneTypeInput.setError(null);
                        descriptionInput.setError(null);
                        weightInput.setError(null);
                        heightInput.setError(null);
                        percentileInput.setError(null);

                        // Save the milestone
                        saveMilestone(milestoneType, description, weight, height, percentile, createAlert);
                        dialog.dismiss();
                    } catch (Exception e) {
                        Toast.makeText(MilestoneActivity.this, "Error processing input: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
            });

            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private boolean isValidNumber(String str) {
        try {
            double num = Double.parseDouble(str);
            return num >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void saveMilestone(String milestoneType, String description, String weight, String height, 
                             String percentile, boolean createAlert) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to save milestones", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate required fields
        if (milestoneType == null || milestoneType.trim().isEmpty()) {
            Toast.makeText(this, "Please select a milestone type", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> milestone = new HashMap<>();
        milestone.put("userId", userId);
        milestone.put("milestoneType", milestoneType.trim());
        milestone.put("description", description != null ? description.trim() : "");
        milestone.put("weight", weight != null ? weight.trim() : "");
        milestone.put("height", height != null ? height.trim() : "");
        milestone.put("percentile", percentile != null ? percentile.trim() : "");
        milestone.put("timestamp", com.google.firebase.Timestamp.now());

        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);

        db.collection("milestones")
            .add(milestone)
            .addOnSuccessListener(documentReference -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Milestone saved successfully", Toast.LENGTH_SHORT).show();

                // Create health alert if requested
                if (createAlert) {
                    createHealthAlertFromMilestone(milestone);
                }

                // Reload the milestones list
                loadMilestones();
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                String errorMessage = "Error saving milestone: ";
                if (e.getMessage().contains("PERMISSION_DENIED")) {
                    errorMessage += "Please check your internet connection and try again.";
                } else {
                    errorMessage += e.getMessage();
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            });
    }

    private void createHealthAlertFromMilestone(Map<String, Object> milestone) {
        String milestoneType = (String) milestone.get("milestoneType");
        String description = (String) milestone.get("description");
        String weight = (String) milestone.get("weight");
        String height = (String) milestone.get("height");
        String percentile = (String) milestone.get("percentile");

        Map<String, Object> alert = new HashMap<>();
        alert.put("userId", mAuth.getCurrentUser().getUid());
        alert.put("alertType", "Milestone");
        alert.put("description", "Milestone Alert: " + milestoneType + "\n" + description);
        if (!weight.isEmpty()) alert.put("weight", weight);
        if (!height.isEmpty()) alert.put("height", height);
        if (!percentile.isEmpty()) alert.put("percentile", percentile);
        alert.put("timestamp", com.google.firebase.Timestamp.now());
        alert.put("reminderEnabled", true);

        db.collection("health_alerts")
            .add(alert)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Health alert created for milestone", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error creating health alert: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void loadMilestones() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to view milestones", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("milestones")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Map<String, Object>> milestones = new ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Map<String, Object> milestone = document.getData();
                    milestone.put("id", document.getId());
                    milestones.add(milestone);
                }
                
                // Set up adapter and display milestones
                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                if (recyclerView.getAdapter() == null) {
                    MilestoneAdapter adapter = new MilestoneAdapter(milestones);
                    recyclerView.setAdapter(adapter);
                } else {
                    ((MilestoneAdapter) recyclerView.getAdapter()).updateMilestones(milestones);
                }
                
                progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading milestones: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            });
    }

    private void fetchOrPromptBirthdate() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists() && doc.contains("birthdate")) {
                userBirthdate = doc.getString("birthdate");
                calculateChildAgeAndShowRecommendations();
            } else {
                promptForBirthdate(userId);
            }
        });
    }

    private void promptForBirthdate(String userId) {
        final TextView birthdateInput = new TextView(this);
        birthdateInput.setPadding(32, 32, 32, 32);
        birthdateInput.setText("Tap to select date");
        birthdateInput.setOnClickListener(v -> {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                String selected = fmt.format(calendar.getTime());
                birthdateInput.setText(selected);
                userBirthdate = selected;
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });
        new android.app.AlertDialog.Builder(this)
            .setTitle("Enter Child's Birthdate")
            .setView(birthdateInput)
            .setCancelable(false)
            .setPositiveButton("Save", (dialog, which) -> {
                if (userBirthdate != null && !userBirthdate.isEmpty()) {
                    db.collection("users").document(userId).update("birthdate", userBirthdate)
                        .addOnSuccessListener(aVoid -> calculateChildAgeAndShowRecommendations());
                } else {
                    Toast.makeText(this, "Please select a birthdate", Toast.LENGTH_SHORT).show();
                    promptForBirthdate(userId);
                }
            })
            .show();
    }

    private void calculateChildAgeAndShowRecommendations() {
        if (userBirthdate == null || userBirthdate.isEmpty()) return;
        try {
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date birth = fmt.parse(userBirthdate);
            java.util.Calendar birthCal = java.util.Calendar.getInstance();
            assert birth != null;
            birthCal.setTime(birth);
            java.util.Calendar now = java.util.Calendar.getInstance();
            int months = (now.get(java.util.Calendar.YEAR) - birthCal.get(java.util.Calendar.YEAR)) * 12 +
                    now.get(java.util.Calendar.MONTH) - birthCal.get(java.util.Calendar.MONTH);
            if (now.get(java.util.Calendar.DAY_OF_MONTH) < birthCal.get(java.util.Calendar.DAY_OF_MONTH)) {
                months--;
            }
            int childAgeMonths = Math.max(0, months);
            showRecommendations(childAgeMonths);
        } catch (Exception e) {
            recommendationsText.setText("");
        }
    }

    private void showRecommendations(int ageMonths) {
        String rec;
        if (ageMonths < 1) {
            rec = "Newborn: Track weight gain and feeding patterns. Expect 6-8 wet diapers daily.";
        } else if (ageMonths < 4) {
            rec = "0-3 months: Monitor weight gain (150-200g/week). Track feeding frequency and duration.";
        } else if (ageMonths < 6) {
            rec = "4-5 months: Watch for doubling of birth weight. Begin tracking readiness for solids.";
        } else if (ageMonths < 9) {
            rec = "6-8 months: Track introduction of solids. Monitor weight gain and feeding patterns.";
        } else if (ageMonths < 12) {
            rec = "9-12 months: Monitor growth rate and feeding milestones. Track self-feeding progress.";
        } else {
            rec = "12+ months: Track growth rate and feeding patterns. Monitor transition to family foods.";
        }
        recommendationsText.setText("Growth & Development Tip: " + rec);
        recommendationsText.setVisibility(View.VISIBLE);
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