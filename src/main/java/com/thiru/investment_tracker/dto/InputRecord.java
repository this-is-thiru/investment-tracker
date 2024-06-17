package com.thiru.investment_tracker.dto;

import java.util.Map;

import com.thiru.investment_tracker.common.parser.CellDetail;

import lombok.Data;

@Data
public class InputRecord {

	private Map<String, CellDetail> record;
	private Integer recordNumber;
}
