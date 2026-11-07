package com.cituccs.sims.controller;

import com.cituccs.sims.service.ItemService;
import com.cituccs.sims.web.ItemModels;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ItemModels.PageResponse<ItemModels.ItemResponse> list(
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {
        return itemService.list(search, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemModels.ItemResponse create(@RequestBody ItemModels.ItemRequest request) {
        return itemService.create(request);
    }

    @PutMapping("/{id}")
    public ItemModels.ItemResponse update(@PathVariable("id") Long id, @RequestBody ItemModels.ItemRequest request) {
        return itemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}