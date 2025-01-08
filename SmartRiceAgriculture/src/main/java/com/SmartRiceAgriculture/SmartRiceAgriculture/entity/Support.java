package com.SmartRiceAgriculture.SmartRiceAgriculture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Support {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userNic;  // NIC of the user who created the ticket

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, length = 1000)
    private String question;

    @Column(length = 1000)
    private String answer;

    private String adminNic;  // NIC of the admin who answered

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime answeredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.OPEN;

    public enum TicketStatus {
        OPEN,       // New ticket, waiting for admin response
        ANSWERED,   // Admin has provided an answer
        CLOSED     // Ticket is closed/resolved
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        if (status == TicketStatus.ANSWERED && answeredAt == null) {
            answeredAt = LocalDateTime.now();
        }
    }
}