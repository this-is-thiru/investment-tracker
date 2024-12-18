package com.thiru.investment_tracker.util.db;

import java.util.ArrayList;
import java.util.List;

import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.Asset;
import com.thiru.investment_tracker.exception.BadRequestException;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import lombok.Data;

@Data
public class QueryFilter {

	private String filterKey;
	private FilterOperation operation;
	private Object value;
	private List<Object> values = new ArrayList<>();
	private LogicalOperation logicalOperation;
	private LogicalOperation expressionLogicalOperation;
	private Boolean allowEmptyOrNull = false;
	private Boolean caseSensitive = false;
	private Boolean isDateField = false;

	public enum FilterOperation {
		EQUALS, NOT_EQUALS, STARTS_WITH, CONTAINS, GREATER_THAN, LESSER_THAN, GREATER_THAN_OR_EQUAL_TO, LESSER_THAN_OR_EQUAL_TO, IS_NULL, IS_NOT_NULL
	}

	public enum LogicalOperation {
		AND, OR
	}

	public static void validateAndSanitizationOfQueryFilters(List<QueryFilter> queryFilters, UserMail userMail) {

		List<QueryFilter> invalidQueryFilters = TCollectionUtil.filter(queryFilters, filter -> Asset.EMAIL.equals(filter.getFilterKey()));

		if (!invalidQueryFilters.isEmpty()) {
			throw new BadRequestException("Email filter is not allowed");
		}

		addEmailToFilter(queryFilters, userMail.getEmail());
		validateFilters(queryFilters);
	}

	private static void addEmailToFilter(List<QueryFilter> queryFilters, String email) {
		QueryFilter queryFilter = new QueryFilter();
		queryFilter.setFilterKey("email");
		queryFilter.setValue(email);
		queryFilter.setOperation(QueryFilter.FilterOperation.EQUALS);
		queryFilters.add(queryFilter);
	}

	private static void validateFilters(List<QueryFilter> queryFilters) {

		List<QueryFilter> invalidQueryFilters = TCollectionUtil.filter(queryFilters,
				filter -> !Asset.ALLOWED_FIELDS.contains(filter.getFilterKey()));

		List<String> invalidFieldsForFilter = TCollectionUtil.map(invalidQueryFilters, QueryFilter::getFilterKey);

		if (!invalidFieldsForFilter.isEmpty()) {
			throw new BadRequestException("These fields are not allowed for filtering: " + invalidFieldsForFilter);
		}
	}
}
