package com.example.hibyassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hibyassistant.R;
import com.example.hibyassistant.models.TrackingEntry;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TrackingEntryAdapter extends RecyclerView.Adapter<TrackingEntryAdapter.ViewHolder> {
    private List<TrackingEntry> entries;
    private SimpleDateFormat timeFormat;

    public TrackingEntryAdapter(List<TrackingEntry> entries) {
        this.entries = entries;
        this.timeFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tracking_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrackingEntry entry = entries.get(position);
        Map<String, Object> data = entry.getData();

        // Set entry type
        String typeText = entry.getType().substring(0, 1).toUpperCase() + 
                         entry.getType().substring(1);
        holder.entryTypeText.setText(typeText);

        // Set time
        holder.entryTimeText.setText(timeFormat.format(entry.getTimestamp()));

        // Set details based on entry type
        StringBuilder details = new StringBuilder();
        switch (entry.getType()) {
            case TrackingEntry.TYPE_FEEDING:
                details.append(data.get("amount")).append(" oz ")
                       .append(data.get("type")).append(" feeding");
                if (data.containsKey("duration")) {
                    details.append(" (").append(data.get("duration")).append(" min)");
                }
                break;
            case TrackingEntry.TYPE_SLEEP:
                String start = data.get("startTime") != null ? data.get("startTime").toString() : null;
                String end = data.get("endTime") != null ? data.get("endTime").toString() : null;
                int durationMin = -1;
                if (start != null && end != null && !start.isEmpty() && !end.isEmpty()) {
                    try {
                        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault());
                        java.util.Date startDate = fmt.parse(start);
                        java.util.Date endDate = fmt.parse(end);
                        if (startDate != null && endDate != null) {
                            long diff = endDate.getTime() - startDate.getTime();
                            if (diff < 0) diff += 24 * 60 * 60 * 1000; // handle overnight sleep
                            durationMin = (int) (diff / (60 * 1000));
                        }
                    } catch (Exception e) {
                        durationMin = -1;
                    }
                }
                details.append("Sleep: ");
                if (durationMin >= 0) {
                    details.append(durationMin).append(" min");
                } else {
                    details.append("-- min");
                }
                break;
            case TrackingEntry.TYPE_DIAPER:
                details.append(data.get("type")).append(" diaper");
                break;
            case TrackingEntry.TYPE_HEALTH:
                if (data.containsKey("temperature")) {
                    details.append("Temperature: ").append(data.get("temperature")).append("°C");
                }
                if (data.containsKey("type")) {
                    if (details.length() > 0) details.append(" - ");
                    details.append(data.get("type"));
                }
                break;
        }
        holder.entryDetailsText.setText(details.toString());

        // Set notes if available
        if (data.containsKey("notes") && data.get("notes") != null) {
            holder.entryNotesText.setVisibility(View.VISIBLE);
            holder.entryNotesText.setText(data.get("notes").toString());
        } else {
            holder.entryNotesText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public void updateEntries(List<TrackingEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView entryTypeText;
        TextView entryTimeText;
        TextView entryDetailsText;
        TextView entryNotesText;

        ViewHolder(View view) {
            super(view);
            entryTypeText = view.findViewById(R.id.entryTypeText);
            entryTimeText = view.findViewById(R.id.entryTimeText);
            entryDetailsText = view.findViewById(R.id.entryDetailsText);
            entryNotesText = view.findViewById(R.id.entryNotesText);
        }
    }
} 