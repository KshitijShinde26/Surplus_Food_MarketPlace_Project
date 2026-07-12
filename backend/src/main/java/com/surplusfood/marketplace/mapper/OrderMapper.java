package com.surplusfood.marketplace.mapper;

import com.surplusfood.marketplace.dto.OrderResponse;
import com.surplusfood.marketplace.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getConsumer().getId(),
                order.getConsumer().getFullName(),
                order.getListing().getId(),
                order.getListing().getName(),
                order.getListing().getBusiness().getId(),
                order.getListing().getBusiness().getBusinessName(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getPickupCode(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
