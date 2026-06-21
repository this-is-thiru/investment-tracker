package com.thiru.wealthlens.dto;

import java.util.Map;

import com.thiru.wealthlens.util.parser.CellDetail;

import lombok.Data;

@Data
public class InputRecord {

	private Map<String, CellDetail> record;
	private Integer recordNumber;
}
