package com.example.hibyassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hibyassistant.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MilestoneAdapter extends RecyclerView.Adapter<MilestoneAdapter.MilestoneViewHolder> {
    private List<Map<String, Object>> milestones;
    private final SimpleDateFormat dateFormat;

    public MilestoneAdapter(List<Map<String, Object>> milestones) {
        this.milestones = milestones;
        this.dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    }

    public void updateMilestones(List<Map<String, Object>> newMilestones) {
        this.milestones = newMilestones;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MilestoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_milestone, parent, false);
        return new MilestoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MilestoneViewHolder holder, int position) {
        Map<String, Object> milestone = milestones.get(position);
        
        // Set milestone type
        String milestoneType = (String) milestone.get("milestoneType");
        holder.typeText.setText(milestoneType);

        // Set description
        String description = (String) milestone.get("description");
        holder.descriptionText.setText(description);

        // Set date
        Timestamp timestamp = (Timestamp) milestone.get("timestamp");
        if (timestamp != null) {
            holder.dateText.setText(dateFormat.format(timestamp.toDate()));
        }

        // Set growth metrics if available
        StringBuilder metricsBuilder = new StringBuilder();
        String weight = (String) milestone.get("weight");
        String height = (String) milestone.get("height");
        String percentile = (String) milestone.get("percentile");

        if (weight != null && !weight.isEmpty()) {
            metricsBuilder.append("Weight: ").append(weight).append(" kg");
        }
        if (height != null && !height.isEmpty()) {
            if (metricsBuilder.length() > 0) metricsBuilder.append(" | ");
            metricsBuilder.append("Height: ").append(height).append(" cm");
        }
        if (percentile != null && !percentile.isEmpty()) {
            if (metricsBuilder.length() > 0) metricsBuilder.append(" | ");
            metricsBuilder.append("Percentile: ").append(percentile).append("%");
        }

        if (metricsBuilder.length() > 0) {
            holder.metricsText.setText(metricsBuilder.toString());
            holder.metricsText.setVisibility(View.VISIBLE);
        } else {
            holder.metricsText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return milestones.size();
    }

    static class MilestoneViewHolder extends RecyclerView.ViewHolder {
        TextView typeText;
        TextView descriptionText;
        TextView dateText;
        TextView metricsText;

        MilestoneViewHolder(View itemView) {
            super(itemView);
            typeText = itemView.findViewById(R.id.milestoneTypeText);
            descriptionText = itemView.findViewById(R.id.milestoneDescriptionText);
            dateText = itemView.findViewById(R.id.milestoneDateText);
            metricsText = itemView.findViewById(R.id.milestoneMetricsText);
        }
    }
} 