package com.cituccs.sims.service;

import com.cituccs.sims.entity.Item;
import com.cituccs.sims.repository.ItemRepository;
import com.cituccs.sims.web.ItemModels;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ItemService {
    private static final int MAX_PAGE_SIZE = 50;

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public ItemModels.PageResponse<ItemModels.ItemResponse> list(String search, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));

        String term = search == null ? "" : search.trim();
        Page<Item> result = term.isEmpty()
                ? itemRepository.findAll(pageable)
                : itemRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(term, term, pageable);

        return new ItemModels.PageResponse<>(
                result.getContent().stream().map(ItemService::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    public ItemModels.ItemResponse create(ItemModels.ItemRequest request) {
        validate(request);
        Item item = new Item();
        apply(item, request);
        return toResponse(itemRepository.save(item));
    }

    public ItemModels.ItemResponse update(Long id, ItemModels.ItemRequest request) {
        validate(request);
        Item item = find(id);
        apply(item, request);
        return toResponse(itemRepository.save(item));
    }

    public void delete(Long id) {
        itemRepository.delete(find(id));
    }

    private Item find(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found."));
    }

    private void validate(ItemModels.ItemRequest request) {
        if (request == null
                || request.name() == null || request.name().isBlank() || request.name().trim().length() > 100
                || (request.category() != null && request.category().trim().length() > 100)
                || request.quantity() == null || request.quantity() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Enter a name (up to 100 characters) and a quantity of 0 or more.");
        }
    }

    private void apply(Item item, ItemModels.ItemRequest request) {
        item.setName(request.name().trim());
        item.setCategory(request.category() == null ? "" : request.category().trim());
        item.setQuantity(request.quantity());
    }

    private static ItemModels.ItemResponse toResponse(Item item) {
        return new ItemModels.ItemResponse(item.getId(), item.getName(), item.getCategory(), item.getQuantity());
    }
}