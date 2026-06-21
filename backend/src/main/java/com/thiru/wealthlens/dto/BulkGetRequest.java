package com.thiru.wealthlens.dto;

import java.util.ArrayList;
import java.util.List;

import com.thiru.wealthlens.dto.DateRange;
import com.thiru.wealthlens.entity.query.QueryFilter;

import lombok.Data;

@Data
public class BulkGetRequest {
	private DateRange dateRange;
	private List<QueryFilter> queryFilters = new ArrayList<>();
}
