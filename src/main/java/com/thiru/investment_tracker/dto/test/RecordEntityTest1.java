package com.thiru.investment_tracker.dto.test;

import java.time.LocalDate;

public record RecordEntityTest1(
        String id,
        String email,
        LocalDate transactionDate
) {
}
