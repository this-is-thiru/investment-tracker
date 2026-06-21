package com.thiru.wealthlens.portfolio.dto.context;

import java.time.LocalDate;

public record BuyContext(double quantity, LocalDate date, double price) {
}
