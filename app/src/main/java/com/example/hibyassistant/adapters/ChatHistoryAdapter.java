package com.example.hibyassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hibyassistant.R;
import com.example.hibyassistant.models.ChatHistory;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder> {

    private List<ChatHistory> chatHistoryList;
    private OnChatLongClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnChatLongClickListener {
        void onChatLongClick(ChatHistory chatHistory);
    }

    public ChatHistoryAdapter(List<ChatHistory> chatHistoryList, OnChatLongClickListener listener) {
        this.chatHistoryList = chatHistoryList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatHistory chatHistory = chatHistoryList.get(position);
        
        // Set user message
        holder.tvUserMessage.setText(chatHistory.getMessage());
        
        // Set AI response
        holder.tvAiResponse.setText(chatHistory.getResponse());
        
        // Set timestamp
        holder.tvTimestamp.setText(dateFormat.format(chatHistory.getTimestamp()));

        // Set click listener for the entire item
        holder.itemView.setOnLongClickListener(v -> {
            listener.onChatLongClick(chatHistory);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return chatHistoryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserMessage;
        TextView tvAiResponse;
        TextView tvTimestamp;

        ViewHolder(View itemView) {
            super(itemView);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
            tvAiResponse = itemView.findViewById(R.id.tvAiResponse);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
} 