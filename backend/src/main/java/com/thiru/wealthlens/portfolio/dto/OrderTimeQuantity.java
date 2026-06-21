package com.thiru.wealthlens.portfolio.dto;

import com.thiru.wealthlens.shared.util.time.TLocalDate;
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
