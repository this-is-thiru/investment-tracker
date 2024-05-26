package com.thiru.investment_tracker.operation;

import org.springframework.data.mongodb.core.query.Criteria;

public class CriteriaBuilder {
     
     public static Criteria constructCriteria(Filter filter) {
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
             case IS_NULL -> criteria.is(null);
             case IS_NOT_NULL -> criteria.ne(null);
         };
         return criteria;
     }
}