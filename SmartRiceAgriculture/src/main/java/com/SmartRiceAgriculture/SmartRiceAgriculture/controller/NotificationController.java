package com.SmartRiceAgriculture.SmartRiceAgriculture.controller;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.AdminBroadcastRequestDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.NotificationRequestDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.NotificationResponseDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification;
import com.SmartRiceAgriculture.SmartRiceAgriculture.mapper.NotificationMapper;
import com.SmartRiceAgriculture.SmartRiceAgriculture.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    @GetMapping("/my")
    public ResponseEntity<?> getMyNotifications(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "User not authenticated"));
            }

            String userNic = authentication.getName();
            List<Notification> notifications = notificationService.getUserNotifications(userNic);

            if (notifications == null || notifications.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<NotificationResponseDTO> dtos = notifications.stream()
                    .map(notification -> notificationMapper.toDTO(notification))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to fetch notifications: " + e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createNotification(@RequestBody NotificationRequestDTO request) {
        try {
            if (request == null || request.getTitle() == null || request.getDescription() == null) {
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("error", "Invalid notification request"));
            }

            Notification notification = notificationService.createNotification(
                    request.getTitle(),
                    request.getDescription(),
                    request.getRecipientNic(),
                    request.getType(),
                    request.getBidId(),
                    request.getOrderId()
            );

            NotificationResponseDTO responseDTO = notificationMapper.toDTO(notification);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to create notification: " + e.getMessage()));
        }
    }

    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createBroadcast(@RequestBody AdminBroadcastRequestDTO request) {
        try {
            if (request == null || request.getTitle() == null || request.getDescription() == null) {
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("error", "Invalid broadcast request"));
            }

            Notification broadcast = notificationService.createBroadcast(
                    request.getTitle(),
                    request.getDescription()
            );

            NotificationResponseDTO responseDTO = notificationMapper.toDTO(broadcast);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to create broadcast: " + e.getMessage()));
        }
    }

    @GetMapping("/broadcasts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllBroadcasts() {
        try {
            List<Notification> broadcasts = notificationService.getAllBroadcasts();

            if (broadcasts == null || broadcasts.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<NotificationResponseDTO> dtos = broadcasts.stream()
                    .map(notification -> notificationMapper.toDTO(notification))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to fetch broadcasts: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok()
                    .body(Collections.singletonMap("message", "Notification deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to delete notification: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "User not authenticated"));
            }

            String userNic = authentication.getName();
            Notification notification = notificationService.markAsRead(id, userNic);
            NotificationResponseDTO responseDTO = notificationMapper.toDTO(notification);

            return ResponseEntity.ok()
                    .body(Collections.singletonMap("message", "Notification marked as read"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to mark notification as read: " + e.getMessage()));
        }
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "User not authenticated"));
            }

            String userNic = authentication.getName();
            notificationService.markAllAsRead(userNic);

            return ResponseEntity.ok()
                    .body(Collections.singletonMap("message", "All notifications marked as read"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to mark all notifications as read: " + e.getMessage()));
        }
    }
}