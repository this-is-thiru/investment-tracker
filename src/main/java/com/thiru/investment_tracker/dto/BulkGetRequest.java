package com.thiru.investment_tracker.dto;

import java.util.ArrayList;
import java.util.List;

import com.thiru.investment_tracker.entity.query.QueryFilter;

import lombok.Data;

@Data
public class BulkGetRequest {
	private DateRange dateRange;
	private List<QueryFilter> queryFilters = new ArrayList<>();
}
