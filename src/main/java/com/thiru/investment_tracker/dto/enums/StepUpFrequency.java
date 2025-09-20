package com.thiru.investment_tracker.dto.enums;

import lombok.Getter;

@Getter
public enum StepUpFrequency {
    MONTHLY(1),
    QUARTERLY(3),
    HALF_YEARLY(6),
    YEARLY(12);

    private final int value;

    StepUpFrequency(int value) {
        this.value = value;
    }

}