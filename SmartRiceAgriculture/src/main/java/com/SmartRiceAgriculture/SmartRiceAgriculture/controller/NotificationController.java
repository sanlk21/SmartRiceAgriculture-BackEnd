package com.SmartRiceAgriculture.SmartRiceAgriculture.controller;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.AdminBroadcastRequestDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.NotificationRequestDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.NotificationResponseDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification;
import com.SmartRiceAgriculture.SmartRiceAgriculture.mapper.NotificationMapper;
import com.SmartRiceAgriculture.SmartRiceAgriculture.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    // Get user's notifications (individual + broadcasts)
    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications(Authentication authentication) {
        String userNic = authentication.getName();
        List<Notification> notifications = notificationService.getUserNotifications(userNic);
        List<NotificationResponseDTO> dtos = notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Create individual notification (for testing or manual creation)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponseDTO> createNotification(
            @RequestBody NotificationRequestDTO request) {
        Notification notification = notificationService.createNotification(
                request.getTitle(),
                request.getDescription(),
                request.getRecipientNic(),
                request.getType(),
                request.getBidId(),
                request.getOrderId()
        );
        return ResponseEntity.ok(notificationMapper.toDTO(notification));
    }

    // Create broadcast notification (admin only)
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponseDTO> createBroadcast(
            @RequestBody AdminBroadcastRequestDTO request) {
        Notification broadcast = notificationService.createBroadcast(
                request.getTitle(),
                request.getDescription()
        );
        return ResponseEntity.ok(notificationMapper.toDTO(broadcast));
    }

    // Get all broadcasts (admin only)
    @GetMapping("/broadcasts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponseDTO>> getAllBroadcasts() {
        List<Notification> broadcasts = notificationService.getAllBroadcasts();
        List<NotificationResponseDTO> dtos = broadcasts.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Delete notification (admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }
}
