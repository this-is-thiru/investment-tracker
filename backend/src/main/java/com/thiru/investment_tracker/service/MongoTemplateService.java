package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.query.QueryBuilder;
import com.thiru.investment_tracker.entity.query.QueryFilter;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Read-only MongoDB query service. Uses {@link MongoTemplate} directly; no
 * class-level @{@link Transactional} because every operation here is a
 * read and wrapping reads in a transaction is unnecessary overhead.
 */
@Log4j2
@AllArgsConstructor
@Service
public class MongoTemplateService {

    private final MongoTemplate mongoTemplate;

    public <T> List<T> getDocuments(UserMail userMail, List<QueryFilter> queryFilters, Class<T> clazz) {

        QueryFilter.validateAndSanitizationOfQueryFilters(queryFilters, userMail);

        QueryBuilder queryBuilder = new QueryBuilder();
        queryFilters.forEach(queryBuilder::addAsCriteria);

        Query query = queryBuilder.build();
        return mongoTemplate.find(query, clazz);
    }
}
