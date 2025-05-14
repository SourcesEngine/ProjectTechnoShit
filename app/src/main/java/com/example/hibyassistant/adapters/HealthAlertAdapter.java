package com.example.hibyassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hibyassistant.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HealthAlertAdapter extends RecyclerView.Adapter<HealthAlertAdapter.ViewHolder> {
    private List<Map<String, Object>> alerts;

    public HealthAlertAdapter(List<Map<String, Object>> alerts) {
        this.alerts = alerts;
    }

    public void updateAlerts(List<Map<String, Object>> newAlerts) {
        this.alerts = newAlerts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_health_alert, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            Map<String, Object> alert = alerts.get(position);
            
            // Set alert type
            String alertType = (String) alert.get("alertType");
            holder.alertTypeText.setText(alertType != null ? alertType : "Unknown Type");
            
            // Set description
            String description = (String) alert.get("description");
            holder.descriptionText.setText(description != null ? description : "");
            
            // Set date if available
            String appointmentDate = (String) alert.get("appointmentDate");
            if (appointmentDate != null && !appointmentDate.isEmpty()) {
                holder.dateText.setVisibility(View.VISIBLE);
                holder.dateText.setText("Date: " + appointmentDate);
            } else {
                holder.dateText.setVisibility(View.GONE);
            }
            
            // Set vaccination type if available
            String vaccinationType = (String) alert.get("vaccinationType");
            if (vaccinationType != null && !vaccinationType.isEmpty()) {
                holder.vaccinationText.setVisibility(View.VISIBLE);
                holder.vaccinationText.setText("Vaccination: " + vaccinationType);
            } else {
                holder.vaccinationText.setVisibility(View.GONE);
            }
            
            // Set illness type if available
            String illnessType = (String) alert.get("illnessType");
            if (illnessType != null && !illnessType.isEmpty()) {
                holder.illnessText.setVisibility(View.VISIBLE);
                holder.illnessText.setText("Illness: " + illnessType);
            } else {
                holder.illnessText.setVisibility(View.GONE);
            }
            
            // Set reminder status
            Boolean reminderEnabled = (Boolean) alert.get("reminderEnabled");
            holder.reminderText.setText("Reminder: " + (reminderEnabled != null && reminderEnabled ? "Enabled" : "Disabled"));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return alerts != null ? alerts.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView alertTypeText;
        TextView descriptionText;
        TextView dateText;
        TextView vaccinationText;
        TextView illnessText;
        TextView reminderText;

        ViewHolder(View view) {
            super(view);
            alertTypeText = view.findViewById(R.id.alertTypeText);
            descriptionText = view.findViewById(R.id.descriptionText);
            dateText = view.findViewById(R.id.dateText);
            vaccinationText = view.findViewById(R.id.vaccinationText);
            illnessText = view.findViewById(R.id.illnessText);
            reminderText = view.findViewById(R.id.reminderText);
        }
    }
} 