package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Support;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.SupportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportService {
    private final SupportRepository supportRepository;

    // Create a new support ticket
    public Support createTicket(String subject, String question) {
        String userNic = SecurityContextHolder.getContext().getAuthentication().getName();

        Support ticket = new Support();
        ticket.setUserNic(userNic);
        ticket.setSubject(subject);
        ticket.setQuestion(question);
        ticket.setStatus(Support.TicketStatus.OPEN);

        return supportRepository.save(ticket);
    }

    // Admin: Answer a ticket
    public Support answerTicket(Long ticketId, String answer) {
        String adminNic = SecurityContextHolder.getContext().getAuthentication().getName();

        Support ticket = supportRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Support ticket not found"));

        ticket.setAnswer(answer);
        ticket.setAdminNic(adminNic);
        ticket.setStatus(Support.TicketStatus.ANSWERED);

        return supportRepository.save(ticket);
    }

    // Close a ticket
    public Support closeTicket(Long ticketId) {
        Support ticket = supportRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Support ticket not found"));

        ticket.setStatus(Support.TicketStatus.CLOSED);
        return supportRepository.save(ticket);
    }

    // Delete a ticket (admin or ticket creator only)
    public void deleteTicket(Long ticketId) {
        String currentUserNic = SecurityContextHolder.getContext().getAuthentication().getName();
        Support ticket = supportRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Support ticket not found"));

        // Check if user is admin or ticket creator
        if (ticket.getUserNic().equals(currentUserNic) ||
                SecurityContextHolder.getContext().getAuthentication()
                        .getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            supportRepository.deleteById(ticketId);
        } else {
            throw new IllegalStateException("Not authorized to delete this ticket");
        }
    }

    // Get user's tickets
    public List<Support> getUserTickets(String userNic) {
        return supportRepository.findByUserNic(userNic);
    }

    // Get all open tickets (admin)
    public List<Support> getOpenTickets() {
        return supportRepository.findByStatus(Support.TicketStatus.OPEN);
    }

    // Get all tickets (admin)
    public List<Support> getAllTickets() {
        return supportRepository.findAll();
    }

    // Get ticket by ID
    public Support getTicketById(Long ticketId) {
        return supportRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Support ticket not found"));
    }
}