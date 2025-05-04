package com.example.hibyassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hibyassistant.R;
import com.example.hibyassistant.models.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private List<Comment> comments;
    private OnCommentLongPressListener longPressListener;

    public interface OnCommentLongPressListener {
        void onCommentLongPress(Comment comment, View view);
    }

    public CommentsAdapter(List<Comment> comments, OnCommentLongPressListener longPressListener) {
        this.comments = comments;
        this.longPressListener = longPressListener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        
        holder.userName.setText(comment.getUserName());
        holder.content.setText(comment.getContent());
        
        // Format and set timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(new Date(comment.getTimestamp()));
        holder.timestamp.setText(formattedDate);

        // Load user profile image
        if (comment.getUserProfileImage() != null && !comment.getUserProfileImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(comment.getUserProfileImage())
                .circleCrop()
                .into(holder.userProfileImage);
        }

        // Set long press listener
        holder.itemView.setOnLongClickListener(v -> {
            if (longPressListener != null) {
                longPressListener.onCommentLongPress(comment, v);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void updateComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView userProfileImage;
        TextView userName;
        TextView content;
        TextView timestamp;

        CommentViewHolder(View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.commentUserProfileImage);
            userName = itemView.findViewById(R.id.commentUserName);
            content = itemView.findViewById(R.id.commentContent);
            timestamp = itemView.findViewById(R.id.commentTimestamp);
        }
    }
} 