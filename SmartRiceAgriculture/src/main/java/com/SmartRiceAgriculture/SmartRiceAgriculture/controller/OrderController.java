package com.SmartRiceAgriculture.SmartRiceAgriculture.controller;


import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.OrderPaymentRequest;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.OrderResponse;
import com.SmartRiceAgriculture.SmartRiceAgriculture.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    // Get order details (Accessible by involved parties and admin)
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @orderService.getOrderDetails(#orderId).buyerNic == authentication.name " +
            "or @orderService.getOrderDetails(#orderId).farmerNic == authentication.name")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderDetails(orderId));
    }

    // Update payment (Buyer only)
    @PostMapping("/{orderId}/payment")
    @PreAuthorize("@orderService.getOrderDetails(#orderId).buyerNic == authentication.name")
    public ResponseEntity<OrderResponse> updatePayment(
            @PathVariable Long orderId,
            @RequestBody OrderPaymentRequest request) {
        return ResponseEntity.ok(orderService.updatePayment(orderId, request));
    }

    // Get buyer's orders (Buyer and Admin only)
    @GetMapping("/buyer/{buyerNic}")
    @PreAuthorize("hasRole('ADMIN') or #buyerNic == authentication.name")
    public ResponseEntity<List<OrderResponse>> getBuyerOrders(@PathVariable String buyerNic) {
        return ResponseEntity.ok(orderService.getBuyerOrders(buyerNic));
    }

    // Get farmer's orders (Farmer and Admin only)
    @GetMapping("/farmer/{farmerNic}")
    @PreAuthorize("hasRole('ADMIN') or #farmerNic == authentication.name")
    public ResponseEntity<List<OrderResponse>> getFarmerOrders(@PathVariable String farmerNic) {
        return ResponseEntity.ok(orderService.getFarmerOrders(farmerNic));
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOrderStatistics() {
        return ResponseEntity.ok(orderService.getOrderStatistics());
    }
}
