package com.example.hibyassistant;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hibyassistant.adapters.TrackingEntryAdapter;
import com.example.hibyassistant.models.TrackingEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TrackingActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TrackingEntryAdapter adapter;
    private List<TrackingEntry> entries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        FloatingActionButton fabAddEntry = findViewById(R.id.fabAddEntry);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Health Tracking");
        }

        // Initialize entries list and adapter
        entries = new ArrayList<>();
        adapter = new TrackingEntryAdapter(entries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set up FAB click listener
        fabAddEntry.setOnClickListener(v -> showAddEntryDialog());

        // Load tracking data
        loadTrackingData();
    }

    private void showAddEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_tracking_entry, null);
        builder.setView(dialogView);

        // Initialize views
        TabLayout tabLayout = dialogView.findViewById(R.id.tabLayout);
        LinearLayout feedingLayout = dialogView.findViewById(R.id.feedingLayout);
        LinearLayout sleepLayout = dialogView.findViewById(R.id.sleepLayout);
        LinearLayout diaperLayout = dialogView.findViewById(R.id.diaperLayout);
        LinearLayout healthLayout = dialogView.findViewById(R.id.healthLayout);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        // Set up tabs
        tabLayout.addTab(tabLayout.newTab().setText("Feeding"));
        tabLayout.addTab(tabLayout.newTab().setText("Sleep"));
        tabLayout.addTab(tabLayout.newTab().setText("Diaper"));
        tabLayout.addTab(tabLayout.newTab().setText("Health"));

        // Show initial layout
        feedingLayout.setVisibility(View.VISIBLE);

        // Set up tab selection listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                feedingLayout.setVisibility(View.GONE);
                sleepLayout.setVisibility(View.GONE);
                diaperLayout.setVisibility(View.GONE);
                healthLayout.setVisibility(View.GONE);

                switch (tab.getPosition()) {
                    case 0:
                        feedingLayout.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        sleepLayout.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        diaperLayout.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        healthLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Set up dropdowns
        setupDropdowns(dialogView);

        // Set up time pickers
        setupTimePickers(dialogView);

        AlertDialog dialog = builder.create();

        // Set up button listeners
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        saveButton.setOnClickListener(v -> {
            int selectedTab = tabLayout.getSelectedTabPosition();
            Map<String, Object> data = new HashMap<>();
            String type;

            switch (selectedTab) {
                case 0: // Feeding
                    type = TrackingEntry.TYPE_FEEDING;
                    data.put("type", ((AutoCompleteTextView) dialogView.findViewById(R.id.feedingTypeInput)).getText().toString());
                    data.put("amount", ((TextInputEditText) dialogView.findViewById(R.id.feedingAmountInput)).getText().toString());
                    data.put("duration", ((TextInputEditText) dialogView.findViewById(R.id.feedingDurationInput)).getText().toString());
                    data.put("notes", ((TextInputEditText) dialogView.findViewById(R.id.feedingNotesInput)).getText().toString());
                    break;
                case 1: // Sleep
                    type = TrackingEntry.TYPE_SLEEP;
                    String startTime = ((TextInputEditText) dialogView.findViewById(R.id.sleepStartTimeInput)).getText().toString();
                    String endTime = ((TextInputEditText) dialogView.findViewById(R.id.sleepEndTimeInput)).getText().toString();
                    data.put("startTime", startTime);
                    data.put("endTime", endTime);
                    data.put("notes", ((TextInputEditText) dialogView.findViewById(R.id.sleepQualityInput)).getText().toString());
                    break;
                case 2: // Diaper
                    type = TrackingEntry.TYPE_DIAPER;
                    data.put("type", ((AutoCompleteTextView) dialogView.findViewById(R.id.diaperTypeInput)).getText().toString());
                    data.put("notes", ((TextInputEditText) dialogView.findViewById(R.id.diaperNotesInput)).getText().toString());
                    break;
                case 3: // Health
                    type = TrackingEntry.TYPE_HEALTH;
                    data.put("type", ((AutoCompleteTextView) dialogView.findViewById(R.id.healthTypeInput)).getText().toString());
                    data.put("temperature", ((TextInputEditText) dialogView.findViewById(R.id.temperatureInput)).getText().toString());
                    data.put("notes", ((TextInputEditText) dialogView.findViewById(R.id.healthNotesInput)).getText().toString());
                    break;
                default:
                    return;
            }

            saveTrackingEntry(type, data);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupDropdowns(View dialogView) {
        // Feeding type dropdown
        String[] feedingTypes = {"Breast Milk", "Formula", "Solids"};
        ArrayAdapter<String> feedingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, feedingTypes);
        ((AutoCompleteTextView) dialogView.findViewById(R.id.feedingTypeInput))
                .setAdapter(feedingAdapter);

        // Diaper type dropdown
        String[] diaperTypes = {"Wet", "Soiled", "Both"};
        ArrayAdapter<String> diaperAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, diaperTypes);
        ((AutoCompleteTextView) dialogView.findViewById(R.id.diaperTypeInput))
                .setAdapter(diaperAdapter);

        // Health type dropdown
        String[] healthTypes = {"Temperature", "Medication", "Vaccination", "Illness", "Other"};
        ArrayAdapter<String> healthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, healthTypes);
        ((AutoCompleteTextView) dialogView.findViewById(R.id.healthTypeInput))
                .setAdapter(healthAdapter);
    }

    private void setupTimePickers(View dialogView) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

        // Sleep start time picker
        TextInputEditText startTimeInput = dialogView.findViewById(R.id.sleepStartTimeInput);
        startTimeInput.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        startTimeInput.setText(timeFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false);
            timePickerDialog.show();
        });

        // Sleep end time picker
        TextInputEditText endTimeInput = dialogView.findViewById(R.id.sleepEndTimeInput);
        endTimeInput.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        endTimeInput.setText(timeFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false);
            timePickerDialog.show();
        });
    }

    private void saveTrackingEntry(String type, Map<String, Object> data) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to save tracking data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate required fields based on entry type
        if (!validateEntryData(type, data)) {
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        TrackingEntry entry = new TrackingEntry(userId, type, data);

        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);

        db.collection("tracking_entries")
                .add(entry.toMap())
                .addOnSuccessListener(documentReference -> {
                    entry.setId(documentReference.getId());
                    entries.add(0, entry);
                    adapter.notifyItemInserted(0);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Entry saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    String errorMessage = "Error saving entry: ";
                    if (e.getMessage().contains("PERMISSION_DENIED")) {
                        errorMessage += "Please check your internet connection and try again.";
                    } else {
                        errorMessage += e.getMessage();
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                });
    }

    private boolean validateEntryData(String type, Map<String, Object> data) {
        switch (type) {
            case TrackingEntry.TYPE_FEEDING:
                if (data.get("type") == null || data.get("type").toString().isEmpty()) {
                    Toast.makeText(this, "Please select a feeding type", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (data.get("amount") == null || data.get("amount").toString().isEmpty()) {
                    Toast.makeText(this, "Please enter the amount", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;

            case TrackingEntry.TYPE_SLEEP:
                if (data.get("startTime") == null || data.get("startTime").toString().isEmpty()) {
                    Toast.makeText(this, "Please enter start time", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (data.get("endTime") == null || data.get("endTime").toString().isEmpty()) {
                    Toast.makeText(this, "Please enter end time", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;

            case TrackingEntry.TYPE_DIAPER:
                if (data.get("type") == null || data.get("type").toString().isEmpty()) {
                    Toast.makeText(this, "Please select a diaper type", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;

            case TrackingEntry.TYPE_HEALTH:
                if (data.get("type") == null || data.get("type").toString().isEmpty()) {
                    Toast.makeText(this, "Please select a health type", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (data.get("temperature") != null && !data.get("temperature").toString().isEmpty()) {
                    try {
                        double temp = Double.parseDouble(data.get("temperature").toString());
                        if (temp < 35 || temp > 42) {
                            Toast.makeText(this, "Please enter a valid temperature (35-42°C)", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Please enter a valid temperature", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                break;
        }
        return true;
    }

    private void loadTrackingData() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to view tracking data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();

        try {
            db.collection("tracking_entries")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (error != null) {
                        Toast.makeText(this, "Error loading tracking data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value == null) {
                        Toast.makeText(this, "No tracking data found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    entries.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : value) {
                        try {
                            Map<String, Object> data = document.getData();
                            if (data != null) {
                                TrackingEntry entry = new TrackingEntry();
                                entry.setId(document.getId());
                                
                                // Safely get and set userId
                                String entryUserId = (String) data.get("userId");
                                entry.setUserId(entryUserId != null ? entryUserId : userId);
                                
                                // Safely get and set type
                                String type = (String) data.get("type");
                                entry.setType(type != null ? type : "");
                                
                                // Safely get and set timestamp
                                Object timestampObj = data.get("timestamp");
                                if (timestampObj instanceof Date) {
                                    entry.setTimestamp((Date) timestampObj);
                                } else if (timestampObj instanceof com.google.firebase.Timestamp) {
                                    entry.setTimestamp(((com.google.firebase.Timestamp) timestampObj).toDate());
                                } else {
                                    entry.setTimestamp(new Date());
                                }
                                
                                // Safely get and set data
                                Object dataObj = data.get("data");
                                if (dataObj instanceof Map) {
                                    entry.setData((Map<String, Object>) dataObj);
                                } else {
                                    entry.setData(new HashMap<>());
                                }
                                
                                entries.add(entry);
                            }
                        } catch (Exception e) {
                            Log.e("TrackingActivity", "Error processing document: " + e.getMessage());
                            continue;
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error initializing tracking data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("TrackingActivity", "Error in loadTrackingData: " + e.getMessage());
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