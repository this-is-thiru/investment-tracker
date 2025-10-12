package com.thiru.investment_tracker.core.dto;

import com.thiru.investment_tracker.core.util.time.TLocalDate;
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
	private String timezoneId = TLocalDate.TIME_ZONE_IST;
}
