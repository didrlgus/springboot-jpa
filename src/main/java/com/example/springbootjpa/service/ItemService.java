package com.example.springbootjpa.service;

import com.example.springbootjpa.domain.item.Item;
import com.example.springbootjpa.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ItemService {

    private final ItemRepository itemRepository;

    /*
     * 상품 추가
     */
    public Long saveItem(Item item) {
        Item result = itemRepository.save(item);
        return result.getId();
    }

    /*
     * 모든 상품 조회
     */
    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    /*
     * 특정 상품 조회
     */
    public Item findOne(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. id=" + itemId));
    }
}
