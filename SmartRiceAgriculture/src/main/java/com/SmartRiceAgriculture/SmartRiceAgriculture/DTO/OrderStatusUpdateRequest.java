package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Order;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    private Order.OrderStatus status;
}
