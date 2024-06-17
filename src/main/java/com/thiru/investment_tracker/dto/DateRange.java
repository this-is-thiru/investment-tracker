package com.thiru.investment_tracker.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thiru.investment_tracker.common.TCommonUtil;

import lombok.Data;

@Data
public class DateRange {

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCommonUtil.DATE_FORMAT)
	private LocalDate startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TCommonUtil.DATE_FORMAT)
	private LocalDate endDate;
}
