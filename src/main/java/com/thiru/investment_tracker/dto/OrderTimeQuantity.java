package com.thiru.investment_tracker.dto;

import com.thiru.investment_tracker.util.collection.TLocaleDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class OrderTimeQuantity {
	private Double quantity;
	private LocalDateTime orderExecutionTime;
	private String timezoneId = TLocaleDate.TIME_ZONE_IST;
}
