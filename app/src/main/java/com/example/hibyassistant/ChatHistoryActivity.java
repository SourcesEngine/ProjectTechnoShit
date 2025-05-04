package com.example.hibyassistant;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hibyassistant.adapters.ChatHistoryAdapter;
import com.example.hibyassistant.models.ChatHistory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatHistoryAdapter adapter;
    private List<ChatHistory> chatHistoryList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        // Check if user is logged in
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to view chat history", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Chat History");

        // Initialize RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatHistoryList = new ArrayList<>();
        adapter = new ChatHistoryAdapter(chatHistoryList, this::showDeleteDialog);
        recyclerView.setAdapter(adapter);

        // Load chat history
        loadChatHistory();
    }

    private void loadChatHistory() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to view chat history", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        progressBar.setVisibility(View.VISIBLE);

        db.collection("chat_history")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                chatHistoryList.clear();
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    String message = document.getString("message");
                    String response = document.getString("response");
                    long timestamp = document.getLong("timestamp");
                    
                    ChatHistory chatHistory = new ChatHistory(
                        document.getId(),
                        userId,
                        message,
                        response,
                        new Date(timestamp)
                    );
                    chatHistoryList.add(chatHistory);
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                Log.e("ChatHistoryActivity", "Error loading chat history", e);
                Toast.makeText(this, "Error loading chat history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    private void showEmptyView(boolean show) {
        emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showDeleteDialog(ChatHistory chatHistory) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Chat")
            .setMessage("Are you sure you want to delete this chat?")
            .setPositiveButton("Delete", (dialog, which) -> deleteChat(chatHistory))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteChat(ChatHistory chatHistory) {
        showLoading(true);
        db.collection("chat_history")
            .document(chatHistory.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                chatHistoryList.remove(chatHistory);
                adapter.notifyDataSetChanged();
                showLoading(false);
                if (chatHistoryList.isEmpty()) {
                    showEmptyView(true);
                }
                Toast.makeText(this, "Chat deleted", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error deleting chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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