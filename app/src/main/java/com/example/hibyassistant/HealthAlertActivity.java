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

import com.example.hibyassistant.adapters.HealthAlertAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import android.app.DatePickerDialog;

public class HealthAlertActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userBirthdate;
    private TextView recommendationsText;
    private RecyclerView recyclerView;
    private HealthAlertAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_alert);

        try {
            // Initialize Firebase
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            // Enable offline persistence
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build();
            db.setFirestoreSettings(settings);

            // Initialize views
            recyclerView = findViewById(R.id.recyclerView);
            progressBar = findViewById(R.id.progressBar);
            FloatingActionButton fabAddAlert = findViewById(R.id.fabAddAlert);
            recommendationsText = findViewById(R.id.recommendationsText);

            // Set up toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Health Alerts");
            }

            // Set up RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new HealthAlertAdapter(new ArrayList<>());
            recyclerView.setAdapter(adapter);

            // Set up FAB click listener
            fabAddAlert.setOnClickListener(v -> showAddAlertDialog());

            // Check if user is signed in
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Please log in to use this feature", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Fetch birthdate and show recommendations
            fetchOrPromptBirthdate();

            // Load health alerts
            loadHealthAlerts();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }
    }

    private void showAddAlertDialog() {
        try {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            View view = getLayoutInflater().inflate(R.layout.dialog_add_health_alert, null);
            builder.setView(view);

            // Fields
            AutoCompleteTextView alertTypeInput = view.findViewById(R.id.alertTypeInput);
            com.google.android.material.textfield.TextInputEditText descriptionInput = view.findViewById(R.id.descriptionInput);
            com.google.android.material.textfield.TextInputEditText appointmentDateInput = view.findViewById(R.id.appointmentDateInput);
            com.google.android.material.textfield.TextInputEditText vaccinationTypeInput = view.findViewById(R.id.vaccinationTypeInput);
            com.google.android.material.textfield.TextInputEditText illnessTypeInput = view.findViewById(R.id.illnessTypeInput);
            SwitchMaterial reminderSwitch = view.findViewById(R.id.reminderSwitch);

            // Set up alert type dropdown
            String[] alertTypes = {"Appointment", "Vaccination", "Illness", "Medication", "Other"};
            ArrayAdapter<String> alertTypeAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, alertTypes);
            alertTypeInput.setAdapter(alertTypeAdapter);
            alertTypeInput.setText("", false); // Clear initial text

            // Date picker for appointment
            appointmentDateInput.setOnClickListener(v -> {
                try {
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    new DatePickerDialog(this, (datePicker, year, month, day) -> {
                        calendar.set(year, month, day);
                        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault());
                        appointmentDateInput.setText(fmt.format(calendar.getTime()));
                    }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error showing date picker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            androidx.appcompat.app.AlertDialog dialog = builder
                .setTitle("Add Health Alert")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .create();

            dialog.setOnShowListener(dialogInterface -> {
                androidx.appcompat.app.AlertDialog alertDialog = (androidx.appcompat.app.AlertDialog) dialogInterface;
                android.widget.Button button = alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view1 -> {
                    try {
                        String alertType = alertTypeInput.getText() != null ? alertTypeInput.getText().toString().trim() : "";
                        String description = descriptionInput.getText() != null ? descriptionInput.getText().toString().trim() : "";
                        String appointmentDate = appointmentDateInput.getText() != null ? appointmentDateInput.getText().toString().trim() : "";
                        String vaccinationType = vaccinationTypeInput.getText() != null ? vaccinationTypeInput.getText().toString().trim() : "";
                        String illnessType = illnessTypeInput.getText() != null ? illnessTypeInput.getText().toString().trim() : "";
                        boolean reminderEnabled = reminderSwitch.isChecked();

                        // Validate required fields
                        if (alertType.isEmpty()) {
                            alertTypeInput.setError("Please select an alert type");
                            return;
                        }

                        if (description.isEmpty()) {
                            descriptionInput.setError("Please enter a description");
                            return;
                        }

                        // Clear any previous errors
                        alertTypeInput.setError(null);
                        descriptionInput.setError(null);

                        // Save the health alert
                        saveHealthAlert(alertType, description, appointmentDate, vaccinationType, illnessType, reminderEnabled);
                        dialog.dismiss();
                    } catch (Exception e) {
                        Toast.makeText(HealthAlertActivity.this, "Error processing input: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void saveHealthAlert(String alertType, String description, String appointmentDate, String vaccinationType, String illnessType, boolean reminderEnabled) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to save health alerts", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String userId = mAuth.getCurrentUser().getUid();
            Map<String, Object> alert = new HashMap<>();
            alert.put("userId", userId);
            alert.put("alertType", alertType);
            alert.put("description", description);
            alert.put("appointmentDate", appointmentDate);
            alert.put("vaccinationType", vaccinationType);
            alert.put("illnessType", illnessType);
            alert.put("reminderEnabled", reminderEnabled);
            alert.put("timestamp", com.google.firebase.Timestamp.now());

            // Show loading indicator
            progressBar.setVisibility(View.VISIBLE);

            db.collection("health_alerts")
                .add(alert)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Health alert saved successfully", Toast.LENGTH_SHORT).show();
                    // Reload the alerts list
                    loadHealthAlerts();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    String errorMessage = "Error saving alert: ";
                    if (e.getMessage().contains("PERMISSION_DENIED")) {
                        errorMessage += "Please check your internet connection and try again.";
                    } else {
                        errorMessage += e.getMessage();
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error saving alert: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void loadHealthAlerts() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to view health alerts", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            progressBar.setVisibility(View.VISIBLE);
            String userId = mAuth.getCurrentUser().getUid();

            db.collection("health_alerts")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        List<Map<String, Object>> alerts = new ArrayList<>();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Map<String, Object> alert = document.getData();
                            alert.put("id", document.getId());
                            alerts.add(alert);
                        }
                        
                        // Update adapter with new alerts
                        adapter.updateAlerts(alerts);
                        progressBar.setVisibility(View.GONE);
                    } catch (Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Error processing alerts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    String errorMessage = "Error loading health alerts: ";
                    if (e.getMessage().contains("PERMISSION_DENIED")) {
                        errorMessage += "Please check your internet connection and try again.";
                    } else {
                        errorMessage += e.getMessage();
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error loading alerts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void fetchOrPromptBirthdate() {
        if (mAuth.getCurrentUser() == null) return;
        try {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("birthdate")) {
                        userBirthdate = doc.getString("birthdate");
                        calculateChildAgeAndShowRecommendations();
                    } else {
                        promptForBirthdate(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching birthdate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } catch (Exception e) {
            Toast.makeText(this, "Error fetching birthdate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void promptForBirthdate(String userId) {
        try {
            final TextView birthdateInput = new TextView(this);
            birthdateInput.setPadding(32, 32, 32, 32);
            birthdateInput.setText("Tap to select date");
            birthdateInput.setOnClickListener(v -> {
                try {
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                        String selected = fmt.format(calendar.getTime());
                        birthdateInput.setText(selected);
                        userBirthdate = selected;
                    }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error showing date picker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            new android.app.AlertDialog.Builder(this)
                .setTitle("Enter Child's Birthdate")
                .setView(birthdateInput)
                .setCancelable(false)
                .setPositiveButton("Save", (dialog, which) -> {
                    if (userBirthdate != null && !userBirthdate.isEmpty()) {
                        db.collection("users").document(userId).update("birthdate", userBirthdate)
                            .addOnSuccessListener(aVoid -> calculateChildAgeAndShowRecommendations())
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error saving birthdate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    } else {
                        Toast.makeText(this, "Please select a birthdate", Toast.LENGTH_SHORT).show();
                        promptForBirthdate(userId);
                    }
                })
                .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing birthdate dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
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
            e.printStackTrace();
        }
    }

    private void showRecommendations(int ageMonths) {
        try {
            String rec;
            if (ageMonths < 1) {
                rec = "Newborn: Feed on demand. Monitor wet diapers and sleep patterns.";
            } else if (ageMonths < 4) {
                rec = "0-3 months: Breast milk or formula only. Expect 6-8 feeds/day.";
            } else if (ageMonths < 6) {
                rec = "4-5 months: Consider increasing formula to 6 oz per feed. Watch for readiness for solids.";
            } else if (ageMonths < 9) {
                rec = "6-8 months: Introduce solids. Continue breast milk/formula. Offer pureed fruits/veggies.";
            } else if (ageMonths < 12) {
                rec = "9-12 months: Encourage self-feeding. Offer soft finger foods. Maintain regular checkups.";
            } else {
                rec = "12+ months: Transition to whole milk. Balanced diet with grains, fruits, veggies, and protein.";
            }
            recommendationsText.setText("Growth & Nutrition Tip: " + rec);
            recommendationsText.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            recommendationsText.setText("");
            e.printStackTrace();
        }
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