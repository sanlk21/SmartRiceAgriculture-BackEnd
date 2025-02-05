package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Order;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Payment;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.OrderRepository;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final NotificationService notificationService;
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

        if (paymentRepository.existsByOrderId(orderId)) {
            throw new IllegalStateException("Payment already exists for this order");
        }

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setBuyerNic(order.getBuyerNic());
        payment.setFarmerNic(order.getFarmerNic());
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentNumber(generatePaymentNumber());
        payment.setStatus(Payment.PaymentStatus.PENDING);

        return paymentRepository.save(payment);
    }

    private Payment.PaymentMethod convertToPaymentMethod(Order.PaymentMethod orderPaymentMethod) {
        return switch (orderPaymentMethod) {
            case BANK_TRANSFER -> Payment.PaymentMethod.BANK_TRANSFER;
            case CASH_ON_DELIVERY -> Payment.PaymentMethod.CASH_ON_DELIVERY;
            case ONLINE_PAYMENT -> Payment.PaymentMethod.ONLINE_PAYMENT;
        };
    }

    private synchronized String generatePaymentNumber() {
        int year = LocalDateTime.now().getYear();
        String yearPrefix = "PAY-" + year + "-";

        Optional<Payment> lastPayment = paymentRepository
                .findFirstByPaymentNumberStartingWithOrderByPaymentNumberDesc(yearPrefix);

        int sequence = 1;
        if (lastPayment.isPresent()) {
            String lastNumber = lastPayment.get().getPaymentNumber();
            try {
                sequence = Integer.parseInt(lastNumber.substring(lastNumber.lastIndexOf('-') + 1)) + 1;
            } catch (Exception e) {
                logger.warn("Error parsing last payment number sequence: {}", e.getMessage());
                sequence = 1;
            }
        }

        return String.format("%s%03d", yearPrefix, sequence);
    }

    @Transactional
    public Payment processPayment(Long paymentId, Map<String, String> paymentDetails, MultipartFile proofDocument) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

            Order order = orderRepository.findById(payment.getOrderId())
                    .orElseThrow(() -> new EntityNotFoundException("Order not found"));

            if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
                throw new IllegalStateException("Payment is not in pending status");
            }

            updatePaymentDetails(payment, paymentDetails);
            handleProofDocument(payment, proofDocument);

            payment.setStatus(Payment.PaymentStatus.PROCESSING);
            Payment savedPayment = paymentRepository.save(payment);

            updateOrderPaymentStatus(order, savedPayment);
            sendPaymentNotifications(order);

            return savedPayment;
        } catch (Exception e) {
            logger.error("Error processing payment {}: {}", paymentId, e.getMessage());
            throw e;
        }
    }

    private void updateOrderPaymentStatus(Order order, Payment payment) {
        order.setStatus(Order.OrderStatus.PAYMENT_COMPLETED);
        order.setPaymentMethod(payment.getPaymentMethod());  // Direct assignment, no conversion needed
        order.setPaymentReference(payment.getTransactionReference());
        order.setPaymentDate(LocalDateTime.now());
        orderRepository.save(order);
    }

    private Order.PaymentMethod convertToOrderPaymentMethod(Payment.PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case BANK_TRANSFER -> Order.PaymentMethod.BANK_TRANSFER;
            case CASH_ON_DELIVERY -> Order.PaymentMethod.CASH_ON_DELIVERY;
            case ONLINE_PAYMENT -> Order.PaymentMethod.ONLINE_PAYMENT;
        };
    }

    private void sendPaymentNotifications(Order order) {
        notificationService.createPaymentNotification(
                order.getBuyerNic(),
                order.getId(),
                order.getOrderNumber(),
                Notification.NotificationType.PAYMENT_RECEIVED,
                order.getTotalAmount()
        );

        notificationService.createPaymentNotification(
                order.getFarmerNic(),
                order.getId(),
                order.getOrderNumber(),
                Notification.NotificationType.PAYMENT_RECEIVED,
                order.getTotalAmount()
        );
    }

    private void handleProofDocument(Payment payment, MultipartFile proofDocument) {
        if (proofDocument != null && !proofDocument.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + proofDocument.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(proofDocument.getInputStream(), filePath);

                payment.setPaymentProofDocumentPath(fileName);
            } catch (IOException e) {
                logger.error("Error handling proof document: {}", e.getMessage());
                throw new RuntimeException("Failed to store proof document", e);
            }
        }
    }

    private void updatePaymentDetails(Payment payment, Map<String, String> details) {
        try {
            switch (payment.getPaymentMethod()) {
                case BANK_TRANSFER -> updateBankTransferDetails(payment, details);
                case CASH_ON_DELIVERY -> updateCODDetails(payment, details);
                case ONLINE_PAYMENT -> updateOnlinePaymentDetails(payment, details);
            }
        } catch (Exception e) {
            logger.error("Error updating payment details: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid payment details provided", e);
        }
    }

    private void updateBankTransferDetails(Payment payment, Map<String, String> details) {
        payment.setSenderBankName(details.get("senderBankName"));
        payment.setSenderAccountNumber(details.get("senderAccountNumber"));
        payment.setSenderAccountName(details.get("senderAccountName"));
        payment.setTransferDate(LocalDateTime.parse(details.get("transferDate")));
    }

    private void updateCODDetails(Payment payment, Map<String, String> details) {
        payment.setDeliveryAddress(details.get("deliveryAddress"));
        payment.setScheduledDeliveryDate(LocalDateTime.parse(details.get("scheduledDeliveryDate")));
        payment.setDeliveryAgentName(details.get("deliveryAgentName"));
        payment.setDeliveryContactNumber(details.get("deliveryContactNumber"));
    }

    private void updateOnlinePaymentDetails(Payment payment, Map<String, String> details) {
        payment.setPaymentGatewayName(details.get("gatewayName"));
        payment.setPaymentGatewayTransactionId(details.get("transactionId"));
        payment.setPaymentGatewayStatus(details.get("gatewayStatus"));
    }

    @Transactional
    public Payment completePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        // Update the payment status
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setCompletedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        // Update the associated order
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        order.setStatus(Order.OrderStatus.PAYMENT_COMPLETED);
        orderRepository.save(order);

        // Send notifications
        notificationService.createPaymentNotification(
                payment.getBuyerNic(),
                payment.getOrderId(),
                order.getOrderNumber(),
                Notification.NotificationType.PAYMENT_COMPLETED,
                payment.getAmount()
        );

        notificationService.createPaymentNotification(
                payment.getFarmerNic(),
                payment.getOrderId(),
                order.getOrderNumber(),
                Notification.NotificationType.PAYMENT_COMPLETED,
                payment.getAmount()
        );

        return savedPayment;
    }

    @Transactional
    public Payment failPayment(Long paymentId, String failureReason) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.recordAttempt(failureReason);

            return paymentRepository.save(payment);
        } catch (Exception e) {
            logger.error("Error failing payment {}: {}", paymentId, e.getMessage());
            throw e;
        }
    }

    // Query methods
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