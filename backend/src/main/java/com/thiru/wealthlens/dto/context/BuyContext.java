package com.thiru.wealthlens.dto.context;

import java.time.LocalDate;

public record BuyContext(double quantity, LocalDate date, double price) {
}
