package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import lombok.Data;

@Data
public class PaymentStatisticsResponse {
    private long totalPayments;
    private long completedPayments;
    private long failedPayments;
    private double totalAmountProcessed;
    private PaymentMethodStats methodStats;

    @Data
    public static class PaymentMethodStats {
        private long bankTransfers;
        private long cashOnDelivery;
        private long onlinePayments;
    }
}
