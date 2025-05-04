package com.example.hibyassistant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.hibyassistant.R;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
    private List<String> mediaUrls;
    private Context context;

    public MediaAdapter(Context context, List<String> mediaUrls) {
        this.context = context;
        this.mediaUrls = mediaUrls;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        String mediaUrl = mediaUrls.get(position);
        
        // Load image using Glide
        Glide.with(context)
            .load(mediaUrl)
            .centerCrop()
            .into(holder.mediaImage);
    }

    @Override
    public int getItemCount() {
        return mediaUrls != null ? mediaUrls.size() : 0;
    }

    public void updateMediaUrls(List<String> newMediaUrls) {
        this.mediaUrls = newMediaUrls;
        notifyDataSetChanged();
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView mediaImage;

        MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            mediaImage = itemView.findViewById(R.id.ivMedia);
        }
    }
} 