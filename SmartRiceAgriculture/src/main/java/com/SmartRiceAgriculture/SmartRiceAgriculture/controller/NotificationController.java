// NotificationController.java
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
    public ResponseEntity<?> getMyNotifications(@RequestHeader(value = "X-User-Nic", required = false) String userNic) {
        try {
            if (userNic == null || userNic.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "User NIC not provided"));
            }

            List<Notification> notifications = notificationService.getUserNotifications(userNic);
            if (notifications == null || notifications.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<NotificationResponseDTO> dtos = notifications.stream()
                    .map(notificationMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to fetch notifications: " + e.getMessage()));
        }
    }

    @PostMapping
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
            return ResponseEntity.ok(notificationMapper.toDTO(notification));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to create notification: " + e.getMessage()));
        }
    }

    @PostMapping("/broadcast")
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
            return ResponseEntity.ok(notificationMapper.toDTO(broadcast));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to create broadcast: " + e.getMessage()));
        }
    }

    @GetMapping("/broadcasts")
    public ResponseEntity<?> getAllBroadcasts() {
        try {
            List<Notification> broadcasts = notificationService.getAllBroadcasts();
            if (broadcasts == null || broadcasts.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<NotificationResponseDTO> dtos = broadcasts.stream()
                    .map(notificationMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to fetch broadcasts: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok(Collections.singletonMap("message", "Notification deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to delete notification: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, @RequestHeader("X-User-Nic") String userNic) {
        try {
            if (userNic == null || userNic.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "User NIC not provided"));
            }

            Notification notification = notificationService.markAsRead(id, userNic);
            return ResponseEntity.ok(Collections.singletonMap("message", "Notification marked as read"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to mark notification as read: " + e.getMessage()));
        }
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@RequestHeader("X-User-Nic") String userNic) {
        try {
            if (userNic == null || userNic.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "User NIC not provided"));
            }

            notificationService.markAllAsRead(userNic);
            return ResponseEntity.ok(Collections.singletonMap("message", "All notifications marked as read"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to mark all notifications as read: " + e.getMessage()));
        }
    }
}