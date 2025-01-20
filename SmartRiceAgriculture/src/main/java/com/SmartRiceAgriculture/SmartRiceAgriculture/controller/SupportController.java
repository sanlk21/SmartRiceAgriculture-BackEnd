package com.SmartRiceAgriculture.SmartRiceAgriculture.controller;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.SupportDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Support;
import com.SmartRiceAgriculture.SmartRiceAgriculture.service.SupportService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SupportController {
    private final SupportService supportService;

    @PostMapping
    public ResponseEntity<?> createTicket(
            @RequestBody SupportDTO.Request request,
            @RequestParam String userNic) {
        try {
            Support ticket = supportService.createTicket(
                    request.getSubject(),
                    request.getQuestion(),
                    userNic
            );
            return ResponseEntity.ok(new SupportDTO.Response(ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{ticketId}/answer")
    public ResponseEntity<?> answerTicket(
            @PathVariable Long ticketId,
            @RequestBody SupportDTO.AnswerRequest request,
            @RequestParam String adminNic) {
        try {
            Support ticket = supportService.answerTicket(ticketId, request.getAnswer(), adminNic);
            return ResponseEntity.ok(new SupportDTO.Response(ticket));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{ticketId}/close")
    public ResponseEntity<?> closeTicket(@PathVariable Long ticketId) {
        try {
            Support ticket = supportService.closeTicket(ticketId);
            return ResponseEntity.ok(new SupportDTO.Response(ticket));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<?> deleteTicket(
            @PathVariable Long ticketId,
            @RequestParam String userNic) {
        try {
            supportService.deleteTicket(ticketId, userNic);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<?> getMyTickets(@RequestParam String userNic) {
        try {
            List<Support> tickets = supportService.getUserTickets(userNic);
            List<SupportDTO.Response> response = tickets.stream()
                    .map(SupportDTO.Response::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/open")
    public ResponseEntity<?> getOpenTickets() {
        try {
            List<Support> tickets = supportService.getOpenTickets();
            List<SupportDTO.Response> response = tickets.stream()
                    .map(SupportDTO.Response::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTickets() {
        try {
            List<Support> tickets = supportService.getAllTickets();
            List<SupportDTO.Response> response = tickets.stream()
                    .map(SupportDTO.Response::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<?> getTicket(@PathVariable Long ticketId) {
        try {
            Support ticket = supportService.getTicketById(ticketId);
            return ResponseEntity.ok(new SupportDTO.Response(ticket));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}