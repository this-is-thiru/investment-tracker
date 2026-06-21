package com.thiru.wealthlens.shared.dto;

import java.util.ArrayList;
import java.util.List;

import com.thiru.wealthlens.shared.dto.DateRange;
import com.thiru.wealthlens.shared.entity.query.QueryFilter;

import lombok.Data;

@Data
public class BulkGetRequest {
	private DateRange dateRange;
	private List<QueryFilter> queryFilters = new ArrayList<>();
}
