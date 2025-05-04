package com.example.hibyassistant;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hibyassistant.models.ChatHistory;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatBotActivity extends AppCompatActivity {
    private static final String TAG = "ChatBotActivity";
    private static final String API_KEY = "AIzaSyDoPnU7GhFMui86s_lyGE9fu1EL09GIzrQ";
    private static final String MODEL_NAME = "gemini-1.5-flash";

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private GenerativeModelFutures model;
    private ExecutorService executor;
    private boolean isProcessing = false;
    private List<String> conversationHistory;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Initialize chat messages list and conversation history
        chatMessages = new ArrayList<>();
        conversationHistory = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Initialize Gemini API
        initializeGeminiAPI();

        // Set up send button click listener
        sendButton.setOnClickListener(v -> {
            if (!isProcessing) {
                sendMessage();
            } else {
                Toast.makeText(this, "Please wait for the current response...", Toast.LENGTH_SHORT).show();
            }
        });

        // Add welcome message
        addBotMessage("Hello! I'm your AI assistant. How can I help you today?");

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeGeminiAPI() {
        try {
            GenerativeModel generativeModel = new GenerativeModel(MODEL_NAME, API_KEY);
            model = GenerativeModelFutures.from(generativeModel);
            Log.d(TAG, "Gemini API initialized successfully");
            executor = Executors.newSingleThreadExecutor();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Gemini API: " + e.getMessage());
            Toast.makeText(this, "Error initializing chatbot: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (!message.isEmpty()) {
            // Clear input
            messageInput.setText("");
            
            // Show user message
            addUserMessage(message);
            
            // Get AI response
            getAIResponse(message);
        }
    }

    private void getAIResponse(String message) {
        isProcessing = true;
        try {
            // Create content with conversation history
            Content.Builder contentBuilder = new Content.Builder();
            
            // Add conversation history
            for (String historyMessage : conversationHistory) {
                contentBuilder.addText(historyMessage);
            }
            
            // Add current message
            contentBuilder.addText(message);
            
            Content content = contentBuilder.build();
            
            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
            Futures.addCallback(
                response,
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        String botResponse = result.getText();
                        if (botResponse == null || botResponse.isEmpty()) {
                            botResponse = "I apologize, but I couldn't generate a response. Please try again.";
                        }
                        
                        // Add messages to conversation history
                        conversationHistory.add(message);
                        conversationHistory.add(botResponse);
                        
                        // Save to chat history
                        saveChatHistory(message, botResponse);
                        
                        // Add bot response to chat
                        addBotMessage(botResponse);
                        isProcessing = false;
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        String errorMessage = "I apologize, but I encountered an error. Please try again.";
                        if (t.getMessage() != null) {
                            if (t.getMessage().contains("429")) {
                                errorMessage = "I'm currently busy. Please try again in a few moments.";
                            } else if (t.getMessage().contains("401") || t.getMessage().contains("403")) {
                                errorMessage = "I'm having trouble connecting. Please check your internet connection.";
                            }
                        }
                        
                        addBotMessage(errorMessage);
                        isProcessing = false;
                        Log.e(TAG, "API call failed: " + t.getMessage(), t);
                    }
                },
                executor
            );
        } catch (Exception e) {
            addBotMessage("I apologize, but I encountered an error. Please try again.");
            isProcessing = false;
            Log.e(TAG, "Error sending message: " + e.getMessage(), e);
        }
    }

    private void saveChatHistory(String message, String response) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to save chat history", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("userId", userId);
        chatData.put("message", message);
        chatData.put("response", response);
        chatData.put("timestamp", System.currentTimeMillis());

        db.collection("chat_history")
            .add(chatData)
            .addOnSuccessListener(documentReference -> {
                Log.d("ChatBotActivity", "Chat history saved successfully");
            })
            .addOnFailureListener(e -> {
                Log.e("ChatBotActivity", "Error saving chat history", e);
                Toast.makeText(this, "Error saving chat history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void addUserMessage(String message) {
        ChatMessage userMessage = new ChatMessage(message, true);
        chatMessages.add(userMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
    }

    private void addBotMessage(String message) {
        runOnUiThread(() -> {
            ChatMessage botMessage = new ChatMessage(message, false);
            chatMessages.add(botMessage);
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
