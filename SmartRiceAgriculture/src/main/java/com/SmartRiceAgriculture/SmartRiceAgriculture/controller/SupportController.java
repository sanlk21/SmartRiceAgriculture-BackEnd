package com.SmartRiceAgriculture.SmartRiceAgriculture.controller;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.SupportDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Support;
import com.SmartRiceAgriculture.SmartRiceAgriculture.service.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;

    // Create new support ticket
    @PostMapping
    public ResponseEntity<SupportDTO.Response> createTicket(@RequestBody SupportDTO.Request request) {
        Support ticket = supportService.createTicket(request.getSubject(), request.getQuestion());
        return ResponseEntity.ok(new SupportDTO.Response(ticket));
    }

    // Answer a ticket (admin only)
    @PostMapping("/{ticketId}/answer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupportDTO.Response> answerTicket(
            @PathVariable Long ticketId,
            @RequestBody SupportDTO.AnswerRequest request) {
        Support ticket = supportService.answerTicket(ticketId, request.getAnswer());
        return ResponseEntity.ok(new SupportDTO.Response(ticket));
    }

    // Close a ticket
    @PutMapping("/{ticketId}/close")
    public ResponseEntity<SupportDTO.Response> closeTicket(@PathVariable Long ticketId) {
        Support ticket = supportService.closeTicket(ticketId);
        return ResponseEntity.ok(new SupportDTO.Response(ticket));
    }

    // Delete a ticket
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long ticketId) {
        supportService.deleteTicket(ticketId);
        return ResponseEntity.ok().build();
    }

    // Get user's tickets
    @GetMapping("/my-tickets")
    public ResponseEntity<List<SupportDTO.Response>> getMyTickets(Authentication authentication) {
        List<Support> tickets = supportService.getUserTickets(authentication.getName());
        List<SupportDTO.Response> response = tickets.stream()
                .map(SupportDTO.Response::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // Get all open tickets (admin only)
    @GetMapping("/open")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SupportDTO.Response>> getOpenTickets() {
        List<Support> tickets = supportService.getOpenTickets();
        List<SupportDTO.Response> response = tickets.stream()
                .map(SupportDTO.Response::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // Get all tickets (admin only)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SupportDTO.Response>> getAllTickets() {
        List<Support> tickets = supportService.getAllTickets();
        List<SupportDTO.Response> response = tickets.stream()
                .map(SupportDTO.Response::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // Get ticket by ID
    @GetMapping("/{ticketId}")
    public ResponseEntity<SupportDTO.Response> getTicket(@PathVariable Long ticketId) {
        Support ticket = supportService.getTicketById(ticketId);
        return ResponseEntity.ok(new SupportDTO.Response(ticket));
    }
}