package com.thiru.investment_tracker.operation;

import com.thiru.investment_tracker.common.CommonUtil;
import org.springframework.data.mongodb.core.query.Criteria;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CriteriaBuilder {

	private static final Object NULL = null;

	public static Criteria constructCriteria(Filter filter) {
		sanitize(filter);

		Filter.FilterOperation operation = filter.getOperation();
		Criteria criteria = new Criteria(filter.getFilterKey());

		criteria = switch (operation) {
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
		};
		return criteria;
	}

	private static void sanitize(Filter filter) {
		if (filter.getIsDateField()) {
			String value = (String) filter.getValue();
			filter.setValue(convertToDate(value));
		}
	}

	private static Date convertToDate(String date) {
		DateFormat dateFormat = new SimpleDateFormat(CommonUtil.DATE_FORMAT);
		try {
			return dateFormat.parse(date);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}
}