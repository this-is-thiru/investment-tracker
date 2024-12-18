package com.thiru.investment_tracker.util.db;

import java.util.Set;

import org.springframework.data.mongodb.core.query.Criteria;

import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import com.thiru.investment_tracker.util.collection.TLocaleDate;

public class CriteriaBuilder {

	private static final Object NULL = null;

	public static void constructCriteria(QueryFilter queryFilter, Set<Criteria> criteriaSet) {
		sanitize(queryFilter);

		QueryFilter.FilterOperation operation = queryFilter.getOperation();
		String filterKey = queryFilter.getFilterKey();

		Criteria queryCriteria = TCollectionUtil.findFirst(criteriaSet, criteria -> filterKey.equals(criteria.getKey()));

		boolean isNewCriteria = false;
		if (queryCriteria == null) {
			queryCriteria = new Criteria(filterKey);
			isNewCriteria = true;
		}

		switch (operation) {
			case EQUALS -> queryCriteria.is(queryFilter.getValue());
			case NOT_EQUALS -> queryCriteria.ne(queryFilter.getValue());
			case GREATER_THAN -> queryCriteria.gt(queryFilter.getValue());
			case LESSER_THAN -> queryCriteria.lt(queryFilter.getValue());
			case GREATER_THAN_OR_EQUAL_TO -> queryCriteria.gte(queryFilter.getValue());
			case LESSER_THAN_OR_EQUAL_TO -> queryCriteria.lte(queryFilter.getValue());
			case STARTS_WITH -> queryCriteria.regex(queryFilter.getValue().toString());
			case CONTAINS -> queryCriteria.in(queryFilter.getValues());
			case IS_NULL -> queryCriteria.is(NULL);
			case IS_NOT_NULL -> queryCriteria.ne(NULL);
		}

		if (isNewCriteria) {
			criteriaSet.add(queryCriteria);
		}
	}

	private static void sanitize(QueryFilter queryFilter) {
		if (queryFilter.getIsDateField()) {
			String value = (String) queryFilter.getValue();
			queryFilter.setValue(TLocaleDate.convertToDate(value));
		}
	}
}
