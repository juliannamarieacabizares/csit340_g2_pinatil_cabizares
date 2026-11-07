package com.cituccs.sims.web;

import java.util.List;

public final class ItemModels {
    private ItemModels() {
    }

    public record ItemRequest(String name, String category, Integer quantity) {
    }

    public record ItemResponse(Long id, String name, String category, int quantity) {
    }

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    }
}