package com.example.hibyassistant.support;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.hibyassistant.R;
import java.util.ArrayList;
import java.util.List;

public class SupportActivity extends AppCompatActivity {
    private EditText subjectInput;
    private EditText descriptionInput;
    private Button createTicketButton;
    private ListView ticketsListView;
    private SupportManager supportManager;
    private SupportTicketAdapter ticketAdapter;
    private String currentUserId; // This should be set from your user authentication system

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Support");

        // Initialize SupportManager
        supportManager = SupportManager.getInstance(this);
        
        // TODO: Set currentUserId from your authentication system
        currentUserId = "user123"; // Replace with actual user ID

        initializeViews();
        setupListeners();
        setupListView();
        loadUserTickets();
    }

    private void initializeViews() {
        subjectInput = findViewById(R.id.subjectInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        createTicketButton = findViewById(R.id.createTicketButton);
        ticketsListView = findViewById(R.id.ticketsListView);
    }

    private void setupListView() {
        // Initialize the adapter with an empty list
        ticketAdapter = new SupportTicketAdapter(this, new ArrayList<>());
        ticketsListView.setAdapter(ticketAdapter);

        // Set click listener for ticket items
        ticketsListView.setOnItemClickListener((parent, view, position, id) -> {
            SupportManager.SupportTicket selectedTicket = ticketAdapter.getItem(position);
            if (selectedTicket != null) {
                showTicketDetails(selectedTicket);
            }
        });
    }

    private void setupListeners() {
        createTicketButton.setOnClickListener(v -> createNewTicket());
    }

    private void createNewTicket() {
        String subject = subjectInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        SupportManager.SupportTicket ticket = supportManager.createTicket(currentUserId, subject, description);
        
        if (ticket != null) {
            // Clear input fields
            subjectInput.setText("");
            descriptionInput.setText("");
            
            // Refresh ticket list
            loadUserTickets();
            
            // Show ticket details
            showTicketDetails(ticket);
        }
    }

    private void loadUserTickets() {
        List<SupportManager.SupportTicket> tickets = supportManager.getUserTickets(currentUserId);
        if (ticketAdapter != null) {
            ticketAdapter.updateTickets(tickets);
        }
    }

    private void showTicketDetails(SupportManager.SupportTicket ticket) {
        // For now, just show a toast with ticket details
        String message = String.format("Ticket: %s\nStatus: %s\nDescription: %s",
            ticket.getSubject(),
            ticket.getStatus(),
            ticket.getDescription());
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void sendMessage(String ticketId, String message) {
        SupportManager.SupportMessage supportMessage = supportManager.addMessage(
            ticketId,
            currentUserId,
            message,
            true // isFromUser
        );

        if (supportMessage != null) {
            // Refresh the ticket list to show updated messages
            loadUserTickets();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}