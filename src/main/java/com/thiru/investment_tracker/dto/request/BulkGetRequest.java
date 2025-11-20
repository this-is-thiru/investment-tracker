package com.thiru.investment_tracker.dto.request;

import java.util.ArrayList;
import java.util.List;

import com.thiru.investment_tracker.dto.helper.DateRange;
import com.thiru.investment_tracker.entity.query.QueryFilter;

import lombok.Data;

@Data
public class BulkGetRequest {
	private DateRange dateRange;
	private List<QueryFilter> queryFilters = new ArrayList<>();
}
