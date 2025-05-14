package com.example.hibyassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.hibyassistant.adapters.SearchResultsAdapter;
import com.example.hibyassistant.models.SearchResult;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SearchResultsAdapter adapter;
    private List<SearchResult> searchResults = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get search query from intent
        String query = getIntent().getStringExtra("search_query");
        if (query == null || query.isEmpty()) {
            finish();
            return;
        }

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search Results");
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchResultsAdapter(searchResults, this::onSearchResultClick);
        recyclerView.setAdapter(adapter);

        // Perform search
        performSearch(query);
    }

    private void performSearch(String query) {
        String userId = mAuth.getCurrentUser().getUid();
        
        // Search in health alerts
        db.collection("users").document(userId)
            .collection("health_alerts")
            .whereGreaterThanOrEqualTo("title", query)
            .whereLessThanOrEqualTo("title", query + "\uf8ff")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (var doc : queryDocumentSnapshots.getDocuments()) {
                    SearchResult result = new SearchResult(
                        doc.getId(),
                        "Health Alert",
                        doc.getString("title"),
                        doc.getString("description"),
                        "health_alert"
                    );
                    searchResults.add(result);
                }
                adapter.updateResults(searchResults);
            });

        // Search in milestones
        db.collection("users").document(userId)
            .collection("milestones")
            .whereGreaterThanOrEqualTo("title", query)
            .whereLessThanOrEqualTo("title", query + "\uf8ff")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (var doc : queryDocumentSnapshots.getDocuments()) {
                    SearchResult result = new SearchResult(
                        doc.getId(),
                        "Milestone",
                        doc.getString("title"),
                        doc.getString("description"),
                        "milestone"
                    );
                    searchResults.add(result);
                }
                adapter.updateResults(searchResults);
            });

        // Search in community posts
        db.collection("posts")
            .whereGreaterThanOrEqualTo("title", query)
            .whereLessThanOrEqualTo("title", query + "\uf8ff")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (var doc : queryDocumentSnapshots.getDocuments()) {
                    SearchResult result = new SearchResult(
                        doc.getId(),
                        "Community Post",
                        doc.getString("title"),
                        doc.getString("content"),
                        "post"
                    );
                    searchResults.add(result);
                }
                adapter.updateResults(searchResults);
            });
    }

    private void onSearchResultClick(SearchResult result) {
        switch (result.getType()) {
            case "health_alert":
                Intent healthAlertIntent = new Intent(this, HealthAlertActivity.class);
                healthAlertIntent.putExtra("alert_id", result.getId());
                startActivity(healthAlertIntent);
                break;
            case "milestone":
                Intent milestoneIntent = new Intent(this, MilestoneActivity.class);
                milestoneIntent.putExtra("milestone_id", result.getId());
                startActivity(milestoneIntent);
                break;
            case "post":
                Intent postIntent = new Intent(this, CommunityForumActivity.class);
                postIntent.putExtra("post_id", result.getId());
                startActivity(postIntent);
                break;
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