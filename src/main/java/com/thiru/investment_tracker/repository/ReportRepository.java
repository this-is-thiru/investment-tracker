package com.thiru.investment_tracker.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.thiru.investment_tracker.entity.Report;

import java.util.List;

public interface ReportRepository extends MongoRepository<Report, String> {

    List<Report> findByEmail(String email);
    void deleteByEmail(String email);
}
