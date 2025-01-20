package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Support;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.SupportRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportService {
    private final SupportRepository supportRepository;

    public Support createTicket(String subject, String question, String userNic) {
        Support ticket = new Support();
        ticket.setUserNic(userNic);
        ticket.setSubject(subject);
        ticket.setQuestion(question);
        ticket.setStatus(Support.TicketStatus.OPEN);
        return supportRepository.save(ticket);
    }

    public Support answerTicket(Long ticketId, String answer, String adminNic) {
        Support ticket = supportRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));

        ticket.setAnswer(answer);
        ticket.setAdminNic(adminNic);
        ticket.setStatus(Support.TicketStatus.ANSWERED);
        return supportRepository.save(ticket);
    }

    public Support closeTicket(Long ticketId) {
        Support ticket = supportRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));

        ticket.setStatus(Support.TicketStatus.CLOSED);
        return supportRepository.save(ticket);
    }

    public void deleteTicket(Long ticketId, String userNic) {
        Support ticket = supportRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));

        if (ticket.getUserNic().equals(userNic) || userNic.startsWith("ADMIN")) {
            supportRepository.deleteById(ticketId);
        } else {
            throw new IllegalStateException("Not authorized to delete this ticket");
        }
    }

    public List<Support> getUserTickets(String userNic) {
        return supportRepository.findByUserNicOrderByCreatedAtDesc(userNic);
    }

    public List<Support> getOpenTickets() {
        return supportRepository.findByStatus(Support.TicketStatus.OPEN);
    }

    public List<Support> getAllTickets() {
        return supportRepository.findAllByOrderByCreatedAtDesc();
    }

    public Support getTicketById(Long ticketId) {
        return supportRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));
    }
}