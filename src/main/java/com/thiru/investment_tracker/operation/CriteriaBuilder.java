package com.thiru.investment_tracker.operation;

import java.util.Set;

import org.springframework.data.mongodb.core.query.Criteria;

import com.thiru.investment_tracker.common.TCommonUtil;
import com.thiru.investment_tracker.common.TLocaleDate;

public class CriteriaBuilder {

	private static final Object NULL = null;

	public static void constructCriteria(Filter filter, Set<Criteria> criteriaSet) {
		sanitize(filter);

		Filter.FilterOperation operation = filter.getOperation();
		String filterKey = filter.getFilterKey();

		Criteria queryCriteria = TCommonUtil.findFirst(criteriaSet, criteria -> filterKey.equals(criteria.getKey()));

		boolean isNewCriteria = false;
		if (queryCriteria == null) {
			queryCriteria = new Criteria(filterKey);
			isNewCriteria = true;
		}

		switch (operation) {
			case EQUALS -> queryCriteria.is(filter.getValue());
			case NOT_EQUALS -> queryCriteria.ne(filter.getValue());
			case GREATER_THAN -> queryCriteria.gt(filter.getValue());
			case LESSER_THAN -> queryCriteria.lt(filter.getValue());
			case GREATER_THAN_OR_EQUAL_TO -> queryCriteria.gte(filter.getValue());
			case LESSER_THAN_OR_EQUAL_TO -> queryCriteria.lte(filter.getValue());
			case STARTS_WITH -> queryCriteria.regex(filter.getValue().toString());
			case CONTAINS -> queryCriteria.in(filter.getValues());
			case IS_NULL -> queryCriteria.is(NULL);
			case IS_NOT_NULL -> queryCriteria.ne(NULL);
		}

		if (isNewCriteria) {
			criteriaSet.add(queryCriteria);
		}
	}

	private static void sanitize(Filter filter) {
		if (filter.getIsDateField()) {
			String value = (String) filter.getValue();
			filter.setValue(TLocaleDate.convertToDate(value));
		}
	}
}