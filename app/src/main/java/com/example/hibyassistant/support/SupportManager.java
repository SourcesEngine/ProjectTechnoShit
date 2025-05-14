package com.example.hibyassistant.support;

import android.content.Context;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SupportManager {
    private Context context;
    private List<SupportTicket> tickets;
    private List<SupportMessage> messages;
    private static SupportManager instance;

    private SupportManager(Context context) {
        this.context = context.getApplicationContext();
        this.tickets = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public static synchronized SupportManager getInstance(Context context) {
        if (instance == null) {
            instance = new SupportManager(context);
        }
        return instance;
    }

    public static class SupportTicket {
        private String ticketId;
        private String userId;
        private String subject;
        private String description;
        private TicketStatus status;
        private long createdAt;
        private long updatedAt;
        private List<SupportMessage> messages;

        public SupportTicket(String userId, String subject, String description) {
            this.ticketId = UUID.randomUUID().toString();
            this.userId = userId;
            this.subject = subject;
            this.description = description;
            this.status = TicketStatus.OPEN;
            this.createdAt = System.currentTimeMillis();
            this.updatedAt = this.createdAt;
            this.messages = new ArrayList<>();
        }

        // Getters
        public String getTicketId() { return ticketId; }
        public String getUserId() { return userId; }
        public String getSubject() { return subject; }
        public String getDescription() { return description; }
        public TicketStatus getStatus() { return status; }
        public long getCreatedAt() { return createdAt; }
        public long getUpdatedAt() { return updatedAt; }
        public List<SupportMessage> getMessages() { return messages; }

        // Setters
        public void setStatus(TicketStatus status) {
            this.status = status;
            this.updatedAt = System.currentTimeMillis();
        }

        public void addMessage(SupportMessage message) {
            this.messages.add(message);
            this.updatedAt = System.currentTimeMillis();
        }
    }

    public static class SupportMessage {
        private String messageId;
        private String ticketId;
        private String senderId;
        private String content;
        private boolean isFromUser;
        private long timestamp;

        public SupportMessage(String ticketId, String senderId, String content, boolean isFromUser) {
            this.messageId = UUID.randomUUID().toString();
            this.ticketId = ticketId;
            this.senderId = senderId;
            this.content = content;
            this.isFromUser = isFromUser;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public String getMessageId() { return messageId; }
        public String getTicketId() { return ticketId; }
        public String getSenderId() { return senderId; }
        public String getContent() { return content; }
        public boolean isFromUser() { return isFromUser; }
        public long getTimestamp() { return timestamp; }
    }

    public enum TicketStatus {
        OPEN,
        IN_PROGRESS,
        RESOLVED,
        CLOSED
    }

    /**
     * Create a new support ticket
     * @param userId The ID of the user creating the ticket
     * @param subject The subject of the ticket
     * @param description The description of the issue
     * @return The created SupportTicket object
     */
    public SupportTicket createTicket(String userId, String subject, String description) {
        if (subject == null || subject.trim().isEmpty()) {
            showToast("Subject cannot be empty");
            return null;
        }

        if (description == null || description.trim().isEmpty()) {
            showToast("Description cannot be empty");
            return null;
        }

        SupportTicket ticket = new SupportTicket(userId, subject, description);
        tickets.add(ticket);
        showToast("Support ticket created successfully");
        return ticket;
    }

    /**
     * Add a message to an existing ticket
     * @param ticketId The ID of the ticket
     * @param senderId The ID of the sender
     * @param content The message content
     * @param isFromUser Whether the message is from the user or support staff
     * @return The created SupportMessage object
     */
    public SupportMessage addMessage(String ticketId, String senderId, String content, boolean isFromUser) {
        if (content == null || content.trim().isEmpty()) {
            showToast("Message cannot be empty");
            return null;
        }

        SupportTicket ticket = getTicketById(ticketId);
        if (ticket == null) {
            showToast("Ticket not found");
            return null;
        }

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            showToast("Cannot add message to a closed ticket");
            return null;
        }

        SupportMessage message = new SupportMessage(ticketId, senderId, content, isFromUser);
        ticket.addMessage(message);
        messages.add(message);
        return message;
    }

    /**
     * Update the status of a ticket
     * @param ticketId The ID of the ticket
     * @param newStatus The new status
     * @return true if the status was updated successfully
     */
    public boolean updateTicketStatus(String ticketId, TicketStatus newStatus) {
        SupportTicket ticket = getTicketById(ticketId);
        if (ticket == null) {
            showToast("Ticket not found");
            return false;
        }

        ticket.setStatus(newStatus);
        return true;
    }

    /**
     * Get all tickets for a user
     * @param userId The ID of the user
     * @return List of tickets for the user
     */
    public List<SupportTicket> getUserTickets(String userId) {
        List<SupportTicket> userTickets = new ArrayList<>();
        for (SupportTicket ticket : tickets) {
            if (ticket.getUserId().equals(userId)) {
                userTickets.add(ticket);
            }
        }
        return userTickets;
    }

    /**
     * Get a ticket by its ID
     * @param ticketId The ID of the ticket
     * @return The SupportTicket object or null if not found
     */
    public SupportTicket getTicketById(String ticketId) {
        for (SupportTicket ticket : tickets) {
            if (ticket.getTicketId().equals(ticketId)) {
                return ticket;
            }
        }
        return null;
    }

    /**
     * Get all messages for a ticket
     * @param ticketId The ID of the ticket
     * @return List of messages for the ticket
     */
    public List<SupportMessage> getTicketMessages(String ticketId) {
        SupportTicket ticket = getTicketById(ticketId);
        return ticket != null ? ticket.getMessages() : new ArrayList<>();
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
} 