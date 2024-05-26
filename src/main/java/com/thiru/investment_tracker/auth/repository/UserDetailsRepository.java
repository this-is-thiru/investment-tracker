package com.thiru.investment_tracker.auth.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.thiru.investment_tracker.auth.entity.UserDetail;

public interface UserDetailsRepository extends MongoRepository<UserDetail, String> {

	Optional<UserDetail> findByEmail(String email);
}