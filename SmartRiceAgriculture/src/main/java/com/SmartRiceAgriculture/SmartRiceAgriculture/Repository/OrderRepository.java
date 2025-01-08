package com.SmartRiceAgriculture.SmartRiceAgriculture.Repository;


import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerNic(String buyerNic);
    List<Order> findByFarmerNic(String farmerNic);
    List<Order> findByStatus(Order.OrderStatus status);
    Order findByOrderNumber(String orderNumber);
    List<Order> findByBidId(Long bidId);
}
