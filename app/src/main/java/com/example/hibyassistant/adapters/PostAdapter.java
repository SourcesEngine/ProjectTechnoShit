package com.example.hibyassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.hibyassistant.R;
import com.example.hibyassistant.models.Post;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts;
    private final OnLikeClickListener likeClickListener;
    private final OnCommentClickListener commentClickListener;
    private final OnProfileClickListener profileClickListener;
    private final OnPostLongPressListener longPressListener;
    private final String currentUserId;

    public interface OnLikeClickListener {
        void onLikeClick(Post post);
    }

    public interface OnCommentClickListener {
        void onCommentClick(Post post);
    }

    public interface OnProfileClickListener {
        void onProfileClick(String userId);
    }

    public interface OnPostLongPressListener {
        void onPostLongPress(Post post, View view);
    }

    public PostAdapter(List<Post> posts, 
                      OnLikeClickListener likeClickListener,
                      OnCommentClickListener commentClickListener,
                      OnProfileClickListener profileClickListener,
                      OnPostLongPressListener longPressListener) {
        this.posts = posts;
        this.likeClickListener = likeClickListener;
        this.commentClickListener = commentClickListener;
        this.profileClickListener = profileClickListener;
        this.longPressListener = longPressListener;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        this.currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        
        // Set user info
        holder.tvUserName.setText(post.getUserName());
        if (post.getUserProfileImage() != null && !post.getUserProfileImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(post.getUserProfileImage())
                .circleCrop()
                .into(holder.ivUserProfile);
        }

        // Set post content
        holder.tvContent.setText(post.getContent());
        
        // Set post image if exists
        if (post.getMediaUrls() != null && !post.getMediaUrls().isEmpty()) {
            holder.ivPostImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                .load(post.getMediaUrls().get(0))
                .into(holder.ivPostImage);
        } else {
            holder.ivPostImage.setVisibility(View.GONE);
        }

        // Set likes and comments count
        holder.tvLikes.setText(String.valueOf(post.getLikes()));
        holder.tvComments.setText(String.valueOf(post.getComments()));

        // Set like button state
        updateLikeButton(holder.btnLike, post);

        // Set click listeners
        holder.btnLike.setOnClickListener(v -> {
            if (likeClickListener != null) {
                likeClickListener.onLikeClick(post);
                updateLikeButton(holder.btnLike, post);
            }
        });

        holder.btnComment.setOnClickListener(v -> {
            if (commentClickListener != null) {
                commentClickListener.onCommentClick(post);
            }
        });

        holder.ivUserProfile.setOnClickListener(v -> {
            if (profileClickListener != null) {
                profileClickListener.onProfileClick(post.getUserId());
            }
        });

        // Set long press listener for post options
        holder.itemView.setOnLongClickListener(v -> {
            if (longPressListener != null && currentUserId != null && post.getUserId().equals(currentUserId)) {
                longPressListener.onPostLongPress(post, v);
                return true;
            }
            return false;
        });
    }

    private void updateLikeButton(ImageButton likeButton, Post post) {
        if (currentUserId == null) {
            likeButton.setImageResource(R.drawable.ic_heart_outline);
            return;
        }
        if (post.isLikedByUser(currentUserId)) {
            likeButton.setImageResource(R.drawable.ic_heart_filled);
        } else {
            likeButton.setImageResource(R.drawable.ic_heart_outline);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void updatePosts(List<Post> newPosts) {
        this.posts = newPosts;
        notifyDataSetChanged();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserProfile;
        TextView tvUserName;
        TextView tvContent;
        ImageView ivPostImage;
        ImageButton btnLike;
        TextView tvLikes;
        ImageButton btnComment;
        TextView tvComments;

        PostViewHolder(View itemView) {
            super(itemView);
            ivUserProfile = itemView.findViewById(R.id.ivUserProfile);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvContent = itemView.findViewById(R.id.tvContent);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            btnLike = itemView.findViewById(R.id.btnLike);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            btnComment = itemView.findViewById(R.id.btnComment);
            tvComments = itemView.findViewById(R.id.tvComments);
        }
    }
} 