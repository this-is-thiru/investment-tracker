package com.thiru.investment_tracker.dto.test;

public record ChangeDTO(
        String entityId,
        String path,
        String field,
        Object oldValue,
        Object newValue
) {}
