package com.example.hibyassistant.support;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.hibyassistant.R;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SupportTicketAdapter extends ArrayAdapter<SupportManager.SupportTicket> {
    private final Context context;
    private final SimpleDateFormat dateFormat;

    public SupportTicketAdapter(Context context, List<SupportManager.SupportTicket> tickets) {
        super(context, R.layout.item_support_ticket, tickets);
        this.context = context;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_support_ticket, parent, false);
        }

        SupportManager.SupportTicket ticket = getItem(position);
        if (ticket != null) {
            TextView subjectView = convertView.findViewById(R.id.ticketSubject);
            TextView statusView = convertView.findViewById(R.id.ticketStatus);
            TextView dateView = convertView.findViewById(R.id.ticketDate);

            subjectView.setText(ticket.getSubject());
            statusView.setText(ticket.getStatus().toString());
            dateView.setText(dateFormat.format(ticket.getCreatedAt()));
        }

        return convertView;
    }

    public void updateTickets(List<SupportManager.SupportTicket> tickets) {
        clear();
        addAll(tickets);
        notifyDataSetChanged();
    }
} 