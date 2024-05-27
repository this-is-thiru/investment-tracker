package com.thiru.investment_tracker.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.thiru.investment_tracker.common.CommonUtil;
import lombok.Data;

@Data
public class DateRange {

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = CommonUtil.DATE_FORMAT)
	private Date startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = CommonUtil.DATE_FORMAT)
	private Date endDate;
}
