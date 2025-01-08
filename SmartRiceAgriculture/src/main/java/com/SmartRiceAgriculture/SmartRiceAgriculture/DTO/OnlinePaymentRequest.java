package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import lombok.Data;

@Data
public class OnlinePaymentRequest {
    private String gatewayName;
    private String transactionId;
    private String gatewayStatus;
}
