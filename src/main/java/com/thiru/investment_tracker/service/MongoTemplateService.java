package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.util.db.CriteriaBuilder;
import com.thiru.investment_tracker.util.db.QueryFilter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class MongoTemplateService {
    private final MongoTemplate mongoTemplate;

    public <T> List<T> getDocuments(UserMail userMail, List<QueryFilter> queryFilters, Class<T> clazz) {

        QueryFilter.validateAndSanitizationOfQueryFilters(queryFilters, userMail);

        Query query = new Query();
        Set<Criteria> criteriaSet = new HashSet<>();

        queryFilters.forEach(filter -> CriteriaBuilder.constructCriteria(filter, criteriaSet));
        criteriaSet.forEach(query::addCriteria);

        return mongoTemplate.find(query, clazz);
    }


}
