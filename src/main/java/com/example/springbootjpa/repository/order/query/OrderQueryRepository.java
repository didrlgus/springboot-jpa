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
}
