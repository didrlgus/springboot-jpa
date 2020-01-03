package com.example.springbootjpa.repository.order.query;

import com.example.springbootjpa.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

// dto 반환 쿼리를 위한 전용 레파지토리
@Repository
public interface OrderQueryRepository extends JpaRepository<Order, Long> {
    @Query("select new com.example.springbootjpa.repository.order.query.OrdersQueryResponseDto(o.id, m.name, o.orderDate, o.status, d.address) " +
            "from Order o " +
            "join o.member m " +
            "join o.delivery d")
    List<OrdersQueryResponseDto> findOrdersDtos();

    // 컬렉션을 제외하고 조인하여 얻어옴
    @Query("select new com.example.springbootjpa.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
            " from Order o" +
            " join o.member m" +
            " join o.delivery d")
    List<OrderQueryDto> findOrderQueryDto();

    // 컬렉션은 따로 조회
    @Query("select new com.example.springbootjpa.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
            " from OrderItem oi" +
            " join oi.item i" +
            " where oi.order.id = :orderId")
    List<OrderItemQueryDto> findOrderItemQueryDto(Long orderId);

    @Query("select new com.example.springbootjpa.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
            " from OrderItem oi" +
            " join oi.item i" +
            " where oi.order.id in :orderIds")      // in으로 한번에 조회
    List<OrderItemQueryDto> findOrderItemQueryDto_Optimize(List<Long> orderIds);

    @Query("select new com.example.springbootjpa.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, d.address, o.status, i.name, oi.orderPrice, oi.count)" +
            " from Order o" +
            " join o.member m" +
            " join o.delivery d" +
            " join o.orderItems oi" +
            " join oi.item i")
    List<OrderFlatDto> findOrderQueryDto_flat();
}
