package com.thiru.investment_tracker.dto;

import java.util.List;

import com.thiru.investment_tracker.util.db.QueryFilter;

import lombok.Data;

@Data
public class BulkGetRequest {
	private DateRange dateRange;
	private List<QueryFilter> queryFilters;
}
