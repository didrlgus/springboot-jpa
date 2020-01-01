package com.example.springbootjpa.service;

import com.example.springbootjpa.domain.item.Book;
import com.example.springbootjpa.domain.item.Item;
import com.example.springbootjpa.repository.ItemRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ItemServiceTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void 상품추가() {
        // given
        Book book = new Book();
        book.setName("자바 ORM 표준 JPA 프로그래밍");
        book.setAuthor("AAA");
        book.setPrice(10000);
        book.setStockQuantity(100);

        // when
        Long itemId = itemService.saveItem(book);

        // then
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("없는 상품입니다."));

        assertThat(item.getName()).isEqualTo("자바 ORM 표준 JPA 프로그래밍");
    }

    @Test
    public void 특정_상품조회() {
        // given
        Book book = new Book();
        book.setName("자바 ORM 표준 JPA 프로그래밍");
        book.setAuthor("AAA");
        book.setPrice(10000);
        book.setStockQuantity(100);

        Long itemId = itemService.saveItem(book);

        // when
        Item item = itemService.findOne(itemId);
        
        // then
        assertThat(item.getId()).isEqualTo(itemId);
    }
}
