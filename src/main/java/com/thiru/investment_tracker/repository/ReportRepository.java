package com.thiru.investment_tracker.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.thiru.investment_tracker.entity.Report;

public interface ReportRepository extends MongoRepository<Report, String> {

    void deleteByEmail(String email);
}
