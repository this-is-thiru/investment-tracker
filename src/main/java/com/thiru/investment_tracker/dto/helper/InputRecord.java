package com.thiru.investment_tracker.dto.helper;

import java.util.Map;

import com.thiru.investment_tracker.util.parser.CellDetail;

import lombok.Data;

@Data
public class InputRecord {

	private Map<String, CellDetail> record;
	private Integer recordNumber;
}
