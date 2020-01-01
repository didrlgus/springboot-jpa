package com.example.springbootjpa.service;

import com.example.springbootjpa.domain.Address;
import com.example.springbootjpa.domain.Member;
import com.example.springbootjpa.domain.Order;
import com.example.springbootjpa.domain.OrderStatus;
import com.example.springbootjpa.domain.item.Book;
import com.example.springbootjpa.domain.item.Item;
import com.example.springbootjpa.exception.NotEnoughStockException;
import com.example.springbootjpa.repository.ItemRepository;
import com.example.springbootjpa.repository.MemberRepository;
import com.example.springbootjpa.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ItemRepository itemRepository;

    @Transactional
    @Test
    public void 상품주문() throws Exception {
        // Given
        Member member = createMember();
        Item item = createBook("JPA BOOK", 10000, 10);      // 이름, 가격, 재고
        int orderCount = 2;

        // when
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        // Then
        Order getOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("없는 주문입니다."));

        assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
        assertEquals("주문한 상품 종류 수가 정확해야 함.", 1, getOrder.getOrderItems().size());
        assertEquals("주문 가격은 가격 * 수량이다.", 10000 * 2, getOrder.getTotalPrice());
        assertEquals("주문 수량만큼 재고가 줄어야 한다.", 8, item.getStockQuantity());
    }

    @Transactional
    @Test(expected = NotEnoughStockException.class)
    public void 상품주문_재고수량초과() throws Exception {

        // Given
        Member member = createMember();
        Item item = createBook("JPA BOOK", 10000, 10);

        int orderCount = 11;    // 재고보다 많은 수량

        // when
        orderService.order(member.getId(), item.getId(), orderCount);

        // then
        fail("재고 수량 부족 예외가 발생");
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        memberRepository.save(member);

        return member;
    }

    private Book createBook(String name, int price, int stockQuantitiy) {
        Book book = new Book();
        book.setName(name);
        book.setStockQuantity(stockQuantitiy);
        book.setPrice(price);
        itemRepository.save(book);

        return book;
    }
}
