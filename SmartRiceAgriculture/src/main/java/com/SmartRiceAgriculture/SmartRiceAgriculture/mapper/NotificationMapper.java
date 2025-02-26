package com.SmartRiceAgriculture.SmartRiceAgriculture.mapper;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.NotificationRequestDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.NotificationResponseDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class NotificationMapper {

    private static final Logger logger = LoggerFactory.getLogger(NotificationMapper.class);

    public Notification toEntity(NotificationRequestDTO dto) {
        if (dto == null) {
            logger.warn("Attempted to map null NotificationRequestDTO");
            return null;
        }

        Notification notification = new Notification();
        notification.setTitle(dto.getTitle());
        notification.setDescription(dto.getDescription());
        notification.setRecipientNic(dto.getRecipientNic());
        notification.setType(dto.getType());
        notification.setBidId(dto.getBidId());
        notification.setOrderId(dto.getOrderId());
        notification.setCreatedDate(LocalDateTime.now());
        notification.setIsRead(false);
        notification.setPriority(
                dto.getType() == Notification.NotificationType.ADMIN_BROADCAST
                        ? Notification.Priority.HIGH
                        : (dto.getPriority() != null ? dto.getPriority() : Notification.Priority.MEDIUM)
        );

        logger.debug("Mapped Notification: title={}, priority={}", notification.getTitle(), notification.getPriority());
        return notification;
    }

    public NotificationResponseDTO toDTO(Notification entity) {
        if (entity == null) {
            logger.warn("Attempted to map null Notification entity");
            return null;
        }

        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setRecipientNic(entity.getRecipientNic());
        dto.setType(entity.getType());
        dto.setPriority(entity.getPriority());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setBidId(entity.getBidId());
        dto.setOrderId(entity.getOrderId());
        dto.setRead(entity.isRead());

        logger.debug("Mapped NotificationResponseDTO: id={}, title={}", dto.getId(), dto.getTitle());
        return dto;
    }
}