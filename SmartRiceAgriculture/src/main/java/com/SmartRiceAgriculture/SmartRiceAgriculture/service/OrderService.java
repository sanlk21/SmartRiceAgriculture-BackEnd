package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.OrderPaymentRequest;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.OrderResponse;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Bid;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Order;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.User;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Payment;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.BidRepository;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.OrderRepository;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;
    private final NotificationService notificationService;
    private final PaymentService paymentService;

    @Transactional
    public OrderResponse createOrder(Long bidId, String buyerNic, String farmerNic,
                                     Float quantity, Float pricePerKg) {
        try {
            validateOrderCreationParams(bidId, buyerNic, farmerNic, quantity, pricePerKg);

            User farmer = userRepository.findById(farmerNic)
                    .orElseThrow(() -> new EntityNotFoundException("Farmer not found with NIC: " + farmerNic));

            Bid bid = validateAndGetBid(bidId);

            Order order = createOrderEntity(bidId, buyerNic, farmerNic, quantity, pricePerKg, farmer, bid);
            Order savedOrder = orderRepository.save(order);

            updateBidStatus(bid);
            sendOrderNotifications(savedOrder, Notification.NotificationType.ORDER_CREATED);

            return convertToResponse(savedOrder);

        } catch (Exception e) {
            logger.error("Error creating order: ", e);
            throw e;
        }
    }

    private void validateOrderCreationParams(Long bidId, String buyerNic, String farmerNic,
                                             Float quantity, Float pricePerKg) {
        if (bidId == null || buyerNic == null || farmerNic == null ||
                quantity == null || pricePerKg == null) {
            throw new IllegalArgumentException("All parameters are required");
        }
    }

    private Bid validateAndGetBid(Long bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new EntityNotFoundException("Bid not found with ID: " + bidId));

        if (bid.getStatus() != Bid.BidStatus.ACTIVE && bid.getStatus() != Bid.BidStatus.ACCEPTED) {
            throw new IllegalStateException("Bid must be ACTIVE or ACCEPTED to create order. Current status: " + bid.getStatus());
        }

        return bid;
    }

    private Order createOrderEntity(Long bidId, String buyerNic, String farmerNic,
                                    Float quantity, Float pricePerKg, User farmer, Bid bid) {
        Order order = new Order();
        order.setBidId(bidId);
        order.setBuyerNic(buyerNic);
        order.setFarmerNic(farmerNic);
        order.setQuantity(quantity);
        order.setPricePerKg(pricePerKg);
        order.setTotalAmount(quantity * pricePerKg);
        order.setOrderDate(LocalDateTime.now());
        order.setPaymentDeadline(LocalDateTime.now().plusHours(24));
        order.setHarvestDateFromBid(bid.getHarvestDate());
        order.setStatus(Order.OrderStatus.PENDING_PAYMENT);

        // Set farmer's bank details
        order.setFarmerBankName(farmer.getBankName());
        order.setFarmerBankBranch(farmer.getBankBranch());
        order.setFarmerAccountNumber(farmer.getAccountNumber());
        order.setFarmerAccountHolderName(farmer.getAccountHolderName());

        return order;
    }

    private void updateBidStatus(Bid bid) {
        if (bid.getStatus() == Bid.BidStatus.ACTIVE) {
            bid.setStatus(Bid.BidStatus.ACCEPTED);
            bidRepository.save(bid);
        }
    }

    @Transactional
    public OrderResponse updatePayment(Long orderId, OrderPaymentRequest request) {
        try {
            Order order = validatePaymentUpdate(orderId);

            Payment payment = paymentService.initializePayment(orderId, request.getPaymentMethod());

            order.updatePaymentDetails(payment);
            Order savedOrder = orderRepository.save(order);

            sendPaymentNotifications(savedOrder);

            return convertToResponse(savedOrder);
        } catch (Exception e) {
            logger.error("Error updating payment: ", e);
            throw e;
        }
    }


    private Order validatePaymentUpdate(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() != Order.OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Order is not in pending payment status");
        }

        if (LocalDateTime.now().isAfter(order.getPaymentDeadline())) {
            throw new IllegalStateException("Payment deadline has passed");
        }

        return order;
    }


    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processOrders() {
        try {
            List<Order> pendingOrders = orderRepository.findByStatus(Order.OrderStatus.PENDING_PAYMENT);
            pendingOrders.forEach(this::processPendingOrder);
        } catch (Exception e) {
            logger.error("Error in order processing scheduler: ", e);
        }
    }

    private void processPendingOrder(Order order) {
        try {
            if (LocalDateTime.now().isAfter(order.getPaymentDeadline())) {
                cancelOrder(order);
            } else if (order.getPaymentDeadline().minusHours(2).isBefore(LocalDateTime.now())) {
                sendPaymentReminder(order);
            }
        } catch (Exception e) {
            logger.error("Error processing order {}: ", order.getId(), e);
        }
    }

    private void cancelOrder(Order order) {
        order.markAsCancelled();
        orderRepository.save(order);

        Bid bid = bidRepository.findById(order.getBidId())
                .orElseThrow(() -> new EntityNotFoundException("Bid not found"));
        bid.setStatus(Bid.BidStatus.ACTIVE);
        bidRepository.save(bid);

        sendOrderNotifications(order, Notification.NotificationType.ORDER_STATUS_CHANGE);
    }

    private void sendPaymentReminder(Order order) {
        notificationService.createPaymentNotification(
                order.getBuyerNic(),
                order.getId(),
                order.getOrderNumber(),
                Notification.NotificationType.PAYMENT_REMINDER,
                order.getTotalAmount()
        );
    }

    private void sendOrderNotifications(Order order, Notification.NotificationType type) {
        notificationService.createOrderNotification(
                order.getBuyerNic(),
                order.getId(),
                order.getOrderNumber(),
                type
        );

        notificationService.createOrderNotification(
                order.getFarmerNic(),
                order.getId(),
                order.getOrderNumber(),
                type
        );
    }

    private void sendPaymentNotifications(Order order) {
        notificationService.createPaymentNotification(
                order.getFarmerNic(),
                order.getId(),
                order.getOrderNumber(),
                Notification.NotificationType.PAYMENT_RECEIVED,
                order.getTotalAmount()
        );

        notificationService.createPaymentNotification(
                order.getBuyerNic(),
                order.getId(),
                order.getOrderNumber(),
                Notification.NotificationType.PAYMENT_RECEIVED,
                order.getTotalAmount()
        );
    }

    // Query Methods
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderDetails(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this::convertToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));
    }

    public List<OrderResponse> getBuyerOrders(String buyerNic) {
        return orderRepository.findByBuyerNic(buyerNic).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getFarmerOrders(String farmerNic) {
        return orderRepository.findByFarmerNic(farmerNic).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

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

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));

            Order.OrderStatus oldStatus = order.getStatus();
            order.setStatus(newStatus);
            Order savedOrder = orderRepository.save(order);

            if (newStatus == Order.OrderStatus.CANCELLED && oldStatus != Order.OrderStatus.CANCELLED) {
                handleOrderCancellation(order);
            }

            sendOrderNotifications(savedOrder, Notification.NotificationType.ORDER_STATUS_CHANGE);

            return convertToResponse(savedOrder);

        } catch (Exception e) {
            logger.error("Error updating order status: ", e);
            throw e;
        }
    }

    private void handleOrderCancellation(Order order) {
        Bid bid = bidRepository.findById(order.getBidId())
                .orElseThrow(() -> new EntityNotFoundException("Bid not found"));
        bid.setStatus(Bid.BidStatus.ACTIVE);
        bidRepository.save(bid);
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
        response.setHarvestDate(LocalDate.from(order.getHarvestDate()));
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