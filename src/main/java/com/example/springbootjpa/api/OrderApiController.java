package com.example.springbootjpa.api;

/*
 * xToOne (ManyToOne, OneToOne)
 * Order
 * Order -> Member 조회
 * Order -> Delivery 조회
 */

import com.example.springbootjpa.domain.Address;
import com.example.springbootjpa.domain.Order;
import com.example.springbootjpa.domain.OrderItem;
import com.example.springbootjpa.domain.OrderStatus;
import com.example.springbootjpa.repository.OrderRepository;
import com.example.springbootjpa.repository.order.query.*;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.yaml.snakeyaml.nodes.NodeId.mapping;

@RequiredArgsConstructor
@RestController
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")   // 지연로딩 시 N+1문제 발생
    public Result<OrdersResponseDto> ordersV1() {
        List<Order> orders = orderRepository.findAll();
        List<OrdersResponseDto> orderDtos
                = orders.stream().map(OrdersResponseDto::new).collect(toList());

        return new Result<>(orderDtos);
    }

    @GetMapping("/api/v2/orders")   // 지연로딩 시 N+1문제 해결 (패치조인 사용), 쿼리 결과를 entity로 받는 방식
    public Result<OrdersResponseDto> ordersV2() {

        // 장점 : 많은 api에서 그래도 활용할 수 있음 (dto만 변경하는 식으로 재활용), 심플함
        // 단점 : entity를 그대로 가져오기때문에 필요없는 데이터까지 다 가져옴

        return new Result<>(orderRepository
                .findAllWithMemberDelivery()
                .stream()
                .map(OrdersResponseDto::new)
                .collect(toList()));
    }

    @GetMapping("/api/v3/orders")
    public Result<OrdersQueryResponseDto> ordersV3() {  // 쿼리 결과를 dto로 받는 방식
        // controll에서 repository 방향으로 OrdersQueryResponseDto를 의존하는 것은 괜찮음
        // repository에서 controller 방향으로 의존성이 잡히면 좋지 않음

        // 장점 : select 절에서 원하는 데이터를 직접 선택하므로 db->애플리케이션 네트워크 용량 최적화 (생각보다 미비)
        // 단점 : repository 재사용성 떨어짐, API 스펙에 맞춘 코드가 repository에 들어가는 단점
        //        (repository는 기본적으로 엔티티를 조회하는데 사용되어야 함) (유연성이 떨어짐)
        // 이럴경우, 별도의 패키지에 repository를 저장해서 query전용 패키지를 별도로 만드는 것이 좋음
        return new Result<>(orderQueryRepository.findOrdersDtos());
    }

    //==== 컬렉션 조회 (item)
    @GetMapping("/api/v1/orders/items")
    public Result<OrdersItemsResponseDto> ordersItemsV1() {     // 총 11개의 쿼리가 나감
        List<Order> orders = orderQueryRepository.findAll();

        List<OrdersItemsResponseDto> result
                = orders.stream().map(OrdersItemsResponseDto::new).collect(toList());

        return new Result<>(result);
    }

    @GetMapping("/api/v2/orders/items")
    public Result<OrdersItemsResponseDto> ordersItemsV2() {
        // 1대다 컬렉션을 조회할 경우 패치 조인을 사용하게 되면 기본적으로 페이징이 불가능하다.
        // 이 경우 db내에서 페이징 처리를 하지 못하고 (조인 시 1대다 에서는 '다'쪽이 기준이 되어서 뭐를 기준으로 페이징할 것인지 db는 알수가 없음)
        // 그래서 애플리케이션 내의 메모리가 페이징을 대신 해준다. (심각한 문제)
        // 데이터가 많아지면 OutOfMemoryError 문제가 발생할 수 있다.
        // (firstResult/maxResults specified with collection fetch; applying in memory!)
        // *toMany 관계는 한번만 패치조인 할 수 있다. 두 depth 이상 들어가면 데이터가 너무 많아져서 예상한 결과를 얻지 못할 수 있음
        // *toOne 관계는 몇 depth라도 계속해서 패치조인 해도 무방하다.
        // 한방 쿼리로 처리하지만 DB자체에서는 중복된 데이터가 너무 많아서 많은 데이터가 애플리케이션으로 전달된다는 단점이 있다.
        Pageable pageable = PageRequest.of(0, 100);

        List<Order> orders = orderRepository.findAllItems(pageable);

        List<OrdersItemsResponseDto> result
                = orders.stream().map(OrdersItemsResponseDto::new).collect(toList());

        return new Result<>(result);
    }

    // 성능 최적화를 하면서 페이징을 하는 방법
    @GetMapping("/api/v3/orders/items")
    public Result<OrdersItemsResponseDto> orderItemV3() {
        // *ToOne 엔티티는 패치조인으로 한방쿼리로 가져온다
        // *ToMany 엔티티는 yml에 글로벌로 batch_size를 설정하면 자동으로 In 쿼리를 날려서 배치사이즈만큼 미리 로딩시킨다
        // batch_size를 사용하면 1+N+M 쿼리가 1+1+1 쿼리로 최적화 된다.
        // 위의 방법에 비해 쿼리는 많이 나가지만 중복된 데이터가 없기때문에 쿼리 한번한번 수행할 때마다 데이터의 용량이 줄어든다.
        // 데이터가 방대해지면 위의 방식보다 더 성능적으로 뛰어날 수도 있다.
        // batch_size는 maximum이 1000이다 (db에 따라 다름)
        // batch_size는 100~1000 사이로 정하는 것을 권장 (db에 따라 sql in절의 파리미터 개수를 1000으로 제한하는 db도 있음)

        Pageable pageable = PageRequest.of(0, 1);   // 하나씩 자름

        List<Order> orders = orderRepository.findAllItemsPaging(pageable);

        List<OrdersItemsResponseDto> result
                = orders.stream().map(OrdersItemsResponseDto::new).collect(toList());

        return new Result<>(result);
    }

    // 컬렉션 쿼리를 dto로 딱 맞춰서 반환하기
    // N+1 문제 발생
    @GetMapping("/api/v4/orders/items")
    public Result<OrderQueryDto> orderItemV4() {
        List<OrderQueryDto> orders = orderQueryRepository.findOrderQueryDto();

        orders.forEach(o -> {
            List<OrderItemQueryDto> orderItems = orderQueryRepository.findOrderItemQueryDto(o.getOrderId());
            o.setOrderItems(orderItems);
        });

        return new Result<>(orders);
    }

    // 컬렉션 쿼리를 dto로 딱 맞춰서 반환할 때 생기는 N+1 문제를 1+1로 줄이는 방법
    @GetMapping("/api/v5/orders/items")
    public Result<OrderQueryDto> orderItemV5() {
        List<OrderQueryDto> orders = orderQueryRepository.findOrderQueryDto();

        List<Long> orderIds
                = orders.stream().map(OrderQueryDto::getOrderId).collect(toList());

        List<OrderItemQueryDto> result
                = orderQueryRepository.findOrderItemQueryDto_Optimize(orderIds);

        // orderId를 키로하고 orderItemList를 값으로 하는 Map을 생성
        // orderId(key)      itemList(value)
        //            1      [....]
        //            2      [....]
        Map<Long, List<OrderItemQueryDto>> orderItemMap
                = result.stream().collect(groupingBy(OrderItemQueryDto::getOrderId));

        orders.forEach(o->o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return new Result<>(orders);
    }

    // 컬렉션 쿼리를 dto로 딱 맞춰서 반환할 때 한방쿼리로 수행하기
    // 장점 : query 1번
    // 단점 : 쿼리는 한번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가 추가되 므로 상황에 따라 V5 보다 더 느릴 수 도 있다.
    //        애플리케이션에서 추가 작업이 크다.
    //        페이징이 불가능하다
    @GetMapping("/api/v6/orders/items")
    public Result<OrderQueryDto> orderItemV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findOrderQueryDto_flat();

        // 반환된 OrderFlatDto 리스트를 직접 OrderQueryDto로 맞춰주기 위한 식
        return new Result<>(flats.stream().collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())))
                .entrySet().stream().map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(),
                e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList()));
    }

    @Data
    static class Result<T> {
        private List<T> data;

        public Result(List<T> orderDtos) {
            data = orderDtos;
        }
    }

    @Data
    static class OrdersResponseDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        // dto에서 entity를 참조하는 것은 괜찮음
        public OrdersResponseDto(Order o) {
            orderId = o.getId();
            name = o.getMember().getName();             // LAZY 초기화
            orderDate = o.getOrderDate();
            orderStatus = o.getStatus();
            address = o.getDelivery().getAddress();     // LAZY 초기화
        }
    }

    @Getter
    static class OrdersItemsResponseDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<ItemsResponseDto> items;

        public OrdersItemsResponseDto(Order o) {
            orderId = o.getId();
            name = o.getMember().getName();
            orderDate = o.getOrderDate();
            orderStatus = o.getStatus();
            address = o.getDelivery().getAddress();
            items = o.getOrderItems().stream().map(ItemsResponseDto::new).collect(Collectors.toList());
        }
    }

    @Getter
    static class ItemsResponseDto {
        private String name;
        private int price;
        private int count;

        public ItemsResponseDto(OrderItem orderItem) {
            name = orderItem.getItem().getName();
            price = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

}
