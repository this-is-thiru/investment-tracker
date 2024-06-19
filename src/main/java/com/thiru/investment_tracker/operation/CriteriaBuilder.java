package com.thiru.investment_tracker.operation;

import java.util.Set;

import org.springframework.data.mongodb.core.query.Criteria;

import com.thiru.investment_tracker.common.TCommonUtil;
import com.thiru.investment_tracker.common.parser.ParserUtil;

public class CriteriaBuilder {

	private static final Object NULL = null;

	public static void constructCriteria(Filter filter, Set<Criteria> criterias) {
		sanitize(filter);

		Filter.FilterOperation operation = filter.getOperation();

		String filterKey = filter.getFilterKey();
		Criteria criteria = TCommonUtil.findFirst(criterias, innercriteria -> filterKey.equals(innercriteria.getKey()),
				new Criteria(filterKey));

		switch (operation) {
			case EQUALS -> criteria.is(filter.getValue());
			case NOT_EQUALS -> criteria.ne(filter.getValue());
			case GREATER_THAN -> criteria.gt(filter.getValue());
			case LESSER_THAN -> criteria.lt(filter.getValue());
			case GREATER_THAN_OR_EQUAL_TO -> criteria.gte(filter.getValue());
			case LESSER_THAN_OR_EQUAL_TO -> criteria.lte(filter.getValue());
			case STARTS_WITH -> criteria.regex(filter.getValue().toString());
			case CONTAINS -> criteria.in(filter.getValues());
			case IS_NULL -> criteria.is(NULL);
			case IS_NOT_NULL -> criteria.ne(NULL);
		}

		Criteria existingCriteria = TCommonUtil.findFirst(criterias,
				innerCriteria -> filterKey.equals(innerCriteria.getKey()));

		if (existingCriteria == null) {
			criterias.add(criteria);
		}
	}

	private static void sanitize(Filter filter) {
		if (filter.getIsDateField()) {
			String value = (String) filter.getValue();
			filter.setValue(ParserUtil.convertToDate(value));
		}
	}
}