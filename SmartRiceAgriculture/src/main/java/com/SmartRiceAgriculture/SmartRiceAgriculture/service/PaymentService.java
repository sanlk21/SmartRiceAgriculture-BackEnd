package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Order;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Payment;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.OrderRepository;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final String UPLOAD_DIR = "uploads/payment-proofs";

    @Transactional
    public Payment initializePayment(Long orderId, Payment.PaymentMethod paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() != Order.OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Order is not in pending payment status");
        }

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setBuyerNic(order.getBuyerNic());
        payment.setFarmerNic(order.getFarmerNic());
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(paymentMethod);

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment processPayment(Long paymentId, Map<String, String> paymentDetails, MultipartFile proofDocument) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not in pending status");
        }

        // Update payment details based on payment method
        updatePaymentDetails(payment, paymentDetails);

        // Handle proof document if provided
        if (proofDocument != null && !proofDocument.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" +
                        proofDocument.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(proofDocument.getInputStream(), filePath);

                payment.setPaymentProofDocumentPath(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store proof document", e);
            }
        }

        payment.setStatus(Payment.PaymentStatus.PROCESSING);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment completePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        payment.markAsCompleted();
        order.setStatus(Order.OrderStatus.PAYMENT_COMPLETED);

        orderRepository.save(order);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment failPayment(Long paymentId, String failureReason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        payment.setStatus(Payment.PaymentStatus.FAILED);
        payment.recordAttempt(failureReason);

        return paymentRepository.save(payment);
    }

    private void updatePaymentDetails(Payment payment, Map<String, String> details) {
        switch (payment.getPaymentMethod()) {
            case BANK_TRANSFER:
                payment.setSenderBankName(details.get("senderBankName"));
                payment.setSenderAccountNumber(details.get("senderAccountNumber"));
                payment.setSenderAccountName(details.get("senderAccountName"));
                payment.setTransferDate(LocalDateTime.parse(details.get("transferDate")));
                break;

            case CASH_ON_DELIVERY:
                payment.setDeliveryAddress(details.get("deliveryAddress"));
                payment.setScheduledDeliveryDate(LocalDateTime.parse(details.get("scheduledDeliveryDate")));
                payment.setDeliveryAgentName(details.get("deliveryAgentName"));
                payment.setDeliveryContactNumber(details.get("deliveryContactNumber"));
                break;

            case ONLINE_PAYMENT:
                payment.setPaymentGatewayName(details.get("gatewayName"));
                payment.setPaymentGatewayTransactionId(details.get("transactionId"));
                payment.setPaymentGatewayStatus(details.get("gatewayStatus"));
                break;
        }
    }

    // Admin methods
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    public List<Payment> getBuyerPayments(String buyerNic) {
        return paymentRepository.findByBuyerNic(buyerNic);
    }

    public List<Payment> getFarmerPayments(String farmerNic) {
        return paymentRepository.findByFarmerNic(farmerNic);
    }

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + paymentId));
    }

    public Map<String, Object> getPaymentStatistics() {
        List<Payment> allPayments = paymentRepository.findAll();

        return Map.of(
                "totalPayments", allPayments.size(),
                "completedPayments", allPayments.stream()
                        .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                        .count(),
                "failedPayments", allPayments.stream()
                        .filter(p -> p.getStatus() == Payment.PaymentStatus.FAILED)
                        .count(),
                "totalAmountProcessed", allPayments.stream()
                        .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                        .mapToDouble(Payment::getAmount)
                        .sum(),
                "paymentMethodDistribution", allPayments.stream()
                        .collect(Collectors.groupingBy(
                                Payment::getPaymentMethod,
                                Collectors.counting()
                        ))
        );
    }
}