package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CashOnDeliveryRequest {
    private String deliveryAddress;
    private LocalDateTime scheduledDeliveryDate;
    private String deliveryAgentName;
    private String deliveryContactNumber;
}
