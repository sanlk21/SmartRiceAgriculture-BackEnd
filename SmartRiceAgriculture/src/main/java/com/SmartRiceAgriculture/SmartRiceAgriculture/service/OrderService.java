package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.OrderPaymentRequest;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.OrderResponse;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Order;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.User;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.OrderRepository;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // Create order from successful bid
    public OrderResponse createOrder(Long bidId, String buyerNic, String farmerNic,
                                     Float quantity, Float pricePerKg) {
        User farmer = userRepository.findById(farmerNic)
                .orElseThrow(() -> new EntityNotFoundException("Farmer not found"));

        Order order = new Order();
        order.setBidId(bidId);
        order.setBuyerNic(buyerNic);
        order.setFarmerNic(farmerNic);
        order.setQuantity(quantity);
        order.setPricePerKg(pricePerKg);
        order.setTotalAmount(quantity * pricePerKg);

        // Set farmer's bank details
        order.setFarmerBankName(farmer.getBankName());
        order.setFarmerBankBranch(farmer.getBankBranch());
        order.setFarmerAccountNumber(farmer.getAccountNumber());
        order.setFarmerAccountHolderName(farmer.getAccountHolderName());

        Order savedOrder = orderRepository.save(order);

        // Notify both parties about order creation
        notificationService.createOrderNotification(
                farmerNic,
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                Notification.NotificationType.ORDER_CREATED
        );

        notificationService.createOrderNotification(
                buyerNic,
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                Notification.NotificationType.ORDER_CREATED
        );

        return convertToResponse(savedOrder);
    }

    // Update payment details
    public OrderResponse updatePayment(Long orderId, OrderPaymentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() != Order.OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Order is not in pending payment status");
        }

        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentReference(request.getPaymentReference());
        order.setPaymentDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PAYMENT_COMPLETED);

        Order savedOrder = orderRepository.save(order);

        // Notify farmer about payment
        notificationService.createPaymentNotification(
                order.getFarmerNic(),
                order.getId(),
                order.getOrderNumber(),
                Notification.NotificationType.PAYMENT_RECEIVED,
                order.getTotalAmount()
        );

        // Notify buyer about payment confirmation
        notificationService.createPaymentNotification(
                order.getBuyerNic(),
                order.getId(),
                order.getOrderNumber(),
                Notification.NotificationType.PAYMENT_RECEIVED,
                order.getTotalAmount()
        );

        return convertToResponse(savedOrder);
    }

    // Check payment deadlines every minute
    @Scheduled(fixedRate = 60000)
    public void processOrders() {
        List<Order> pendingOrders = orderRepository.findByStatus(Order.OrderStatus.PENDING_PAYMENT);

        for(Order order : pendingOrders) {
            if(LocalDateTime.now().isAfter(order.getPaymentDeadline())) {
                order.setStatus(Order.OrderStatus.CANCELLED);
                orderRepository.save(order);

                // Notify both parties about cancellation
                notificationService.createOrderNotification(
                        order.getBuyerNic(),
                        order.getId(),
                        order.getOrderNumber(),
                        Notification.NotificationType.ORDER_STATUS_CHANGE
                );

                notificationService.createOrderNotification(
                        order.getFarmerNic(),
                        order.getId(),
                        order.getOrderNumber(),
                        Notification.NotificationType.ORDER_STATUS_CHANGE
                );
            } else if(order.getPaymentDeadline().minusHours(2).isBefore(LocalDateTime.now())) {
                // Send payment reminder 2 hours before deadline
                notificationService.createPaymentNotification(
                        order.getBuyerNic(),
                        order.getId(),
                        order.getOrderNumber(),
                        Notification.NotificationType.PAYMENT_REMINDER,
                        order.getTotalAmount()
                );
            }
        }
    }

    // Admin: Get all orders
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Admin: Get order statistics
    public Map<String, Object> getOrderStatistics() {
        List<Order> allOrders = orderRepository.findAll();

        return Map.of(
                "totalOrders", allOrders.size(),
                "pendingPayments", allOrders.stream()
                        .filter(o -> o.getStatus() == Order.OrderStatus.PENDING_PAYMENT).count(),
                "completedOrders", allOrders.stream()
                        .filter(o -> o.getStatus() == Order.OrderStatus.PAYMENT_COMPLETED).count(),
                "cancelledOrders", allOrders.stream()
                        .filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED).count(),
                "totalRevenue", allOrders.stream()
                        .filter(o -> o.getStatus() == Order.OrderStatus.PAYMENT_COMPLETED)
                        .mapToDouble(Order::getTotalAmount)
                        .sum(),
                "totalQuantitySold", allOrders.stream()
                        .filter(o -> o.getStatus() == Order.OrderStatus.PAYMENT_COMPLETED)
                        .mapToDouble(Order::getQuantity)
                        .sum()
        );
    }

    // Get buyer's orders
    public List<OrderResponse> getBuyerOrders(String buyerNic) {
        return orderRepository.findByBuyerNic(buyerNic).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get farmer's orders
    public List<OrderResponse> getFarmerOrders(String farmerNic) {
        return orderRepository.findByFarmerNic(farmerNic).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get single order details
    public OrderResponse getOrderDetails(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        return convertToResponse(order);
    }

    // Update order status (admin function)
    public OrderResponse updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        // Notify both parties about status change
        notificationService.createOrderNotification(
                order.getBuyerNic(),
                order.getId(),
                order.getOrderNumber(),
                Notification.NotificationType.ORDER_STATUS_CHANGE
        );

        notificationService.createOrderNotification(
                order.getFarmerNic(),
                order.getId(),
                order.getOrderNumber(),
                Notification.NotificationType.ORDER_STATUS_CHANGE
        );

        return convertToResponse(savedOrder);
    }

    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setBidId(order.getBidId());
        response.setBuyerNic(order.getBuyerNic());
        response.setFarmerNic(order.getFarmerNic());
        response.setQuantity(order.getQuantity());
        response.setPricePerKg(order.getPricePerKg());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderDate(order.getOrderDate());
        response.setPaymentDeadline(order.getPaymentDeadline());
        response.setFarmerBankName(order.getFarmerBankName());
        response.setFarmerBankBranch(order.getFarmerBankBranch());
        response.setFarmerAccountNumber(order.getFarmerAccountNumber());
        response.setFarmerAccountHolderName(order.getFarmerAccountHolderName());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentReference(order.getPaymentReference());
        response.setPaymentDate(order.getPaymentDate());
        response.setStatus(order.getStatus());
        return response;
    }
}