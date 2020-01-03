package com.example.springbootjpa.repository.order.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class OrderItemQueryDto {

    @JsonIgnore
    private Long orderId;       //주문번호
    private String itemName;    //상품명
    private int orderPrice;     //주문가격
    private int itemCount;

    public OrderItemQueryDto(Long orderId, String itemName, int orderPrice, int itemCount) {
        this.orderId = orderId;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.itemCount = itemCount;
    }
}
