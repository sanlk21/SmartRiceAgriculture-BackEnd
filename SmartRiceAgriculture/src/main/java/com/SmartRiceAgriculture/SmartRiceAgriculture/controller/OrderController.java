// OrderController.java
package com.SmartRiceAgriculture.SmartRiceAgriculture.controller;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.OrderPaymentRequest;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.OrderResponse;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Order;
import com.SmartRiceAgriculture.SmartRiceAgriculture.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @orderService.getOrderDetails(#orderId).buyerNic == authentication.name " +
            "or @orderService.getOrderDetails(#orderId).farmerNic == authentication.name")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable Long orderId) {
        logger.info("Fetching order details for ID: {}", orderId);
        return ResponseEntity.ok(orderService.getOrderDetails(orderId));
    }

    @PostMapping("/{orderId}/payment")
    @PreAuthorize("@orderService.getOrderDetails(#orderId).buyerNic == authentication.name")
    public ResponseEntity<OrderResponse> updatePayment(
            @PathVariable Long orderId,
            @RequestBody OrderPaymentRequest request) {
        logger.info("Updating payment for order ID: {}", orderId);
        return ResponseEntity.ok(orderService.updatePayment(orderId, request));
    }

    @GetMapping("/buyer/{buyerNic}")
    @PreAuthorize("hasRole('ADMIN') or #buyerNic == authentication.name")
    public ResponseEntity<List<OrderResponse>> getBuyerOrders(@PathVariable String buyerNic) {
        logger.info("Fetching orders for buyerNic: {}", buyerNic);
        return ResponseEntity.ok(orderService.getBuyerOrders(buyerNic));
    }

    @GetMapping("/farmer/{farmerNic}")
    @PreAuthorize("hasRole('ADMIN') or #farmerNic == authentication.name")
    public ResponseEntity<List<OrderResponse>> getFarmerOrders(@PathVariable String farmerNic) {
        logger.info("Fetching orders for farmerNic: {}", farmerNic);
        List<OrderResponse> orders = orderService.getFarmerOrders(farmerNic);
        if (orders.isEmpty()) {
            logger.warn("No orders found for farmerNic: {}", farmerNic);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(orders);
        }
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        logger.info("Fetching all orders");
        List<OrderResponse> orders = orderService.getAllOrders();
        if (orders.isEmpty()) {
            logger.warn("No orders found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(orders);
        }
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOrderStatistics() {
        logger.info("Fetching order statistics");
        return ResponseEntity.ok(orderService.getOrderStatistics());
    }

    @PutMapping("/admin/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Order.OrderStatus status) {
        logger.info("Admin updating order status: {} to {}", orderId, status);
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    @PutMapping("/admin/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        logger.info("Admin cancelling order: {}", orderId);
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, Order.OrderStatus.CANCELLED));
    }
}
