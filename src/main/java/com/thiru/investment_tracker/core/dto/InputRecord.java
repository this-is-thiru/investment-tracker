package com.thiru.investment_tracker.core.dto;

import java.util.Map;

import com.thiru.investment_tracker.core.util.parser.CellDetail;

import lombok.Data;

@Data
public class InputRecord {

	private Map<String, CellDetail> record;
	private Integer recordNumber;
}
