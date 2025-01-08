package com.SmartRiceAgriculture.SmartRiceAgriculture.mapper;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.*;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Payment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setPaymentNumber(payment.getPaymentNumber());
        response.setOrderId(payment.getOrderId());
        response.setBuyerNic(payment.getBuyerNic());
        response.setFarmerNic(payment.getFarmerNic());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        response.setTransactionReference(payment.getTransactionReference());
        response.setCreatedAt(payment.getCreatedAt());
        response.setCompletedAt(payment.getCompletedAt());
        response.setAttemptCount(payment.getAttemptCount());

        PaymentDetailsResponse details = new PaymentDetailsResponse();
        switch (payment.getPaymentMethod()) {
            case BANK_TRANSFER:
                details.setSenderBankName(payment.getSenderBankName());
                details.setSenderAccountNumber(payment.getSenderAccountNumber());
                details.setSenderAccountName(payment.getSenderAccountName());
                details.setTransferDate(payment.getTransferDate());
                break;

            case CASH_ON_DELIVERY:
                details.setDeliveryAddress(payment.getDeliveryAddress());
                details.setScheduledDeliveryDate(payment.getScheduledDeliveryDate());
                details.setDeliveryAgentName(payment.getDeliveryAgentName());
                details.setDeliveryContactNumber(payment.getDeliveryContactNumber());
                break;

            case ONLINE_PAYMENT:
                details.setPaymentGatewayName(payment.getPaymentGatewayName());
                details.setPaymentGatewayTransactionId(payment.getPaymentGatewayTransactionId());
                details.setPaymentGatewayStatus(payment.getPaymentGatewayStatus());
                break;
        }
        response.setDetails(details);

        return response;
    }

    public Map<String, String> toBankTransferDetails(BankTransferRequest request) {
        Map<String, String> details = new HashMap<>();
        details.put("senderBankName", request.getSenderBankName());
        details.put("senderAccountNumber", request.getSenderAccountNumber());
        details.put("senderAccountName", request.getSenderAccountName());
        details.put("transferDate", request.getTransferDate().toString());
        return details;
    }

    public Map<String, String> toCashOnDeliveryDetails(CashOnDeliveryRequest request) {
        Map<String, String> details = new HashMap<>();
        details.put("deliveryAddress", request.getDeliveryAddress());
        details.put("scheduledDeliveryDate", request.getScheduledDeliveryDate().toString());
        details.put("deliveryAgentName", request.getDeliveryAgentName());
        details.put("deliveryContactNumber", request.getDeliveryContactNumber());
        return details;
    }

    public Map<String, String> toOnlinePaymentDetails(OnlinePaymentRequest request) {
        Map<String, String> details = new HashMap<>();
        details.put("gatewayName", request.getGatewayName());
        details.put("transactionId", request.getTransactionId());
        details.put("gatewayStatus", request.getGatewayStatus());
        return details;
    }

    public PaymentStatisticsResponse toStatisticsResponse(Map<String, Object> stats) {
        PaymentStatisticsResponse response = new PaymentStatisticsResponse();
        response.setTotalPayments((Long) stats.get("totalPayments"));
        response.setCompletedPayments((Long) stats.get("completedPayments"));
        response.setFailedPayments((Long) stats.get("failedPayments"));
        response.setTotalAmountProcessed((Double) stats.get("totalAmountProcessed"));

        Map<Payment.PaymentMethod, Long> methodDistribution =
                (Map<Payment.PaymentMethod, Long>) stats.get("paymentMethodDistribution");

        PaymentStatisticsResponse.PaymentMethodStats methodStats =
                new PaymentStatisticsResponse.PaymentMethodStats();
        methodStats.setBankTransfers(methodDistribution.getOrDefault(Payment.PaymentMethod.BANK_TRANSFER, 0L));
        methodStats.setCashOnDelivery(methodDistribution.getOrDefault(Payment.PaymentMethod.CASH_ON_DELIVERY, 0L));
        methodStats.setOnlinePayments(methodDistribution.getOrDefault(Payment.PaymentMethod.ONLINE_PAYMENT, 0L));

        response.setMethodStats(methodStats);
        return response;
    }
}
