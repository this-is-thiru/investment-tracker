package com.thiru.investment_tracker.dto.context;

import java.time.LocalDate;

public record BuyContext(double quantity, LocalDate date, double price) {
}
