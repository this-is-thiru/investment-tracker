package com.thiru.wealthlens.taxplanning.salary.repository;

import com.thiru.wealthlens.taxplanning.salary.entity.SalaryProfileEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SalaryProfileRepository extends MongoRepository<SalaryProfileEntity, String> {

    List<SalaryProfileEntity> findByEmailOrderByCreatedDateDesc(String email);
}