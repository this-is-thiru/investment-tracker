package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.query.QueryBuilder;
import com.thiru.investment_tracker.entity.query.QueryFilter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
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
