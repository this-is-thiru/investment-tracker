package com.thiru.investment_tracker.dto.context;

import java.time.LocalDate;

public record BuyContext(LocalDate buyDate, double buyPrice) {
}
