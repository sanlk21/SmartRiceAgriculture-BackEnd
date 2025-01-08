package com.SmartRiceAgriculture.SmartRiceAgriculture.mapper;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.NotificationRequestDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.NotificationResponseDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notification toEntity(NotificationRequestDTO dto) {
        Notification notification = new Notification();
        notification.setTitle(dto.getTitle());
        notification.setDescription(dto.getDescription());
        notification.setRecipientNic(dto.getRecipientNic());
        notification.setType(dto.getType());
        notification.setBidId(dto.getBidId());
        notification.setOrderId(dto.getOrderId());
        return notification;
    }

    public NotificationResponseDTO toDTO(Notification entity) {
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
        return dto;
    }
}