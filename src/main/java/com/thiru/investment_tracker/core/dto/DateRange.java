package com.thiru.investment_tracker.core.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.thiru.investment_tracker.core.util.collection.TCollectionUtil;

import lombok.Data;

@Data
public class DateRange {

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
	private LocalDate startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCollectionUtil.DATE_FORMAT)
	private LocalDate endDate;
}
