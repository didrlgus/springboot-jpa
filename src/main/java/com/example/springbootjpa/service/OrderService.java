package com.example.springbootjpa.service;

import com.example.springbootjpa.domain.Delivery;
import com.example.springbootjpa.domain.Member;
import com.example.springbootjpa.domain.Order;
import com.example.springbootjpa.domain.OrderItem;
import com.example.springbootjpa.domain.item.Item;
import com.example.springbootjpa.repository.MemberRepository;
import com.example.springbootjpa.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class OrderService {

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ItemService itemService;

    /*
     * 주문
     */
    public Long order(Long memberId, Long itemId, int count) {

        // 엔티티 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Item item = itemService.findOne(itemId);

        // 배송정보 생성
        Delivery delivery = new Delivery(member.getAddress());
        // 주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
        // 주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);
        // 주문 저장
        orderRepository.save(order);

        return order.getId();
    }

    /*
     * 주문 취소
     */
    public void cancelOrder(Long orderId) {

        // 주문 엔티티 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        order.cancel();
    }
}
