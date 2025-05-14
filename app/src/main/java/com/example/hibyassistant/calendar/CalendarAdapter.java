package com.example.hibyassistant.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.hibyassistant.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CalendarAdapter extends BaseAdapter {
    private final List<String> days;
    private final LayoutInflater inflater;
    private final FirebaseFirestore db;
    private int currentMonth;
    private int currentYear;

    public CalendarAdapter(Context context, List<String> days) {
        this.days = days;
        this.inflater = LayoutInflater.from(context);
        this.db = FirebaseFirestore.getInstance();
    }

    public void setCurrentMonth(int month) {
        this.currentMonth = month;
    }

    public void setCurrentYear(int year) {
        this.currentYear = year;
    }

    @Override
    public int getCount() {
        return days.size();
    }

    @Override
    public Object getItem(int position) {
        return days.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_calendar_day, parent, false);
        }

        TextView dayText = view.findViewById(R.id.dayText);
        View eventIndicator = view.findViewById(R.id.eventIndicator);

        String day = days.get(position);
        dayText.setText(day);

        // Reset visibility
        eventIndicator.setVisibility(View.GONE);

        if (!day.isEmpty()) {
            // Check for events on this day
            checkForEvents(Integer.parseInt(day), eventIndicator);
        }

        return view;
    }

    private void checkForEvents(int day, View eventIndicator) {
        // Query Firestore for events on this day
        db.collection("events")
            .whereEqualTo("day", day)
            .whereEqualTo("month", currentMonth)
            .whereEqualTo("year", currentYear)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    eventIndicator.setVisibility(View.VISIBLE);
                }
            });
    }
} 