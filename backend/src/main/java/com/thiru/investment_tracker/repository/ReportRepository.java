package com.thiru.investment_tracker.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.thiru.investment_tracker.entity.ReportEntity;

import java.util.List;

public interface ReportRepository extends MongoRepository<ReportEntity, String> {

    List<ReportEntity> findByEmail(String email);
    void deleteByEmail(String email);
}
