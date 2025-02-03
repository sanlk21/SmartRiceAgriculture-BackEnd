package com.SmartRiceAgriculture.SmartRiceAgriculture.controller;


import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.*;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Payment;
import com.SmartRiceAgriculture.SmartRiceAgriculture.mapper.PaymentMapper;
import com.SmartRiceAgriculture.SmartRiceAgriculture.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    // Initialize a new payment for an order
    @PostMapping("/initialize")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<PaymentResponse> initializePayment(@RequestBody PaymentInitRequest request) {
        Payment payment = paymentService.initializePayment(
                request.getOrderId(),
                request.getPaymentMethod()
        );
        return ResponseEntity.ok(paymentMapper.toResponse(payment));
    }

    // Process bank transfer payment
    @PostMapping("/{paymentId}/bank-transfer")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<PaymentResponse> processBankTransfer(
            @PathVariable Long paymentId,
            @RequestPart("request") BankTransferRequest request,
            @RequestPart(value = "proof", required = false) MultipartFile proof) {

        Payment payment = paymentService.processPayment(
                paymentId,
                paymentMapper.toBankTransferDetails(request),
                proof
        );
        return ResponseEntity.ok(paymentMapper.toResponse(payment));
    }

    // Process cash on delivery payment
    @PostMapping("/{paymentId}/cash-on-delivery")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<PaymentResponse> processCashOnDelivery(
            @PathVariable Long paymentId,
            @RequestBody CashOnDeliveryRequest request) {

        Payment payment = paymentService.processPayment(
                paymentId,
                paymentMapper.toCashOnDeliveryDetails(request),
                null
        );
        return ResponseEntity.ok(paymentMapper.toResponse(payment));
    }

    // Process online payment
    @PostMapping("/{paymentId}/online-payment")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<PaymentResponse> processOnlinePayment(
            @PathVariable Long paymentId,
            @RequestBody OnlinePaymentRequest request) {

        Payment payment = paymentService.processPayment(
                paymentId,
                paymentMapper.toOnlinePaymentDetails(request),
                null
        );
        return ResponseEntity.ok(paymentMapper.toResponse(payment));
    }

    // Get payment details
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('BUYER', 'FARMER', 'ADMIN')")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long paymentId) {
        Payment payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(paymentMapper.toResponse(payment));
    }

    // Get buyer's payments
    @GetMapping("/buyer/{buyerNic}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<List<PaymentResponse>> getBuyerPayments(@PathVariable String buyerNic) {
        List<Payment> payments = paymentService.getBuyerPayments(buyerNic);
        return ResponseEntity.ok(
                payments.stream()
                        .map(paymentMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    // Get farmer's payments
    @GetMapping("/farmer/{farmerNic}")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<List<PaymentResponse>> getFarmerPayments(@PathVariable String farmerNic) {
        List<Payment> payments = paymentService.getFarmerPayments(farmerNic);
        return ResponseEntity.ok(
                payments.stream()
                        .map(paymentMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    // Admin Endpoints

    // Get all payments
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(
                payments.stream()
                        .map(paymentMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    // Get payments by status
    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(
            @PathVariable Payment.PaymentStatus status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(
                payments.stream()
                        .map(paymentMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    // Get payment statistics
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentStatisticsResponse> getPaymentStatistics() {
        return ResponseEntity.ok(
                paymentMapper.toStatisticsResponse(
                        paymentService.getPaymentStatistics()
                )
        );
    }

    // Admin: Manually complete a payment
    @PostMapping("/admin/{paymentId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> completePayment(@PathVariable Long paymentId) {
        Payment payment = paymentService.completePayment(paymentId);
        return ResponseEntity.ok(paymentMapper.toResponse(payment));
    }

    // Admin: Mark payment as failed
    @PostMapping("/admin/{paymentId}/fail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> failPayment(
            @PathVariable Long paymentId,
            @RequestParam String reason) {
        Payment payment = paymentService.failPayment(paymentId, reason);
        return ResponseEntity.ok(paymentMapper.toResponse(payment));
    }
}
