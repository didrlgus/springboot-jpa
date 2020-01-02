package com.example.springbootjpa.repository;

import com.example.springbootjpa.domain.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("select o from Order o join fetch o.member m join fetch o.delivery d")
    List<Order> findAllWithMemberDelivery();

    @Query("select distinct o from Order o" +
            " join fetch o.member m" +
            " join fetch o.delivery d" +
            " join fetch o.orderItems oi" +
            " join fetch oi.item i")
    List<Order> findAllItems(Pageable pageable);

    // *ToOne 엔티티는 패치조인으로 한방쿼리로 가져온다
    // *ToMany 엔티티는 yml에 글로벌로 batch_size를 설정하면 자동으로 In 쿼리를 날려서 배치사이즈만큼 미리 로딩시킨다
    // batch_size를 사용하면 1+N+M 쿼리가 1+1+1 쿼리로 최적화 된다.
    @Query("select o from Order o" +
            " join fetch o.member m" +
            " join fetch o.delivery d")
    List<Order> findAllItemsPaging(Pageable pageable);
}
