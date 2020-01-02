package com.example.springbootjpa.repository.order.query;

import com.example.springbootjpa.domain.Address;
import com.example.springbootjpa.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrdersQueryResponseDto {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    public OrdersQueryResponseDto(Long orderId, String name, LocalDateTime orderDate,
                                  OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }
}
