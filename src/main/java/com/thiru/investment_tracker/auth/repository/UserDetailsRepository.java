package com.thiru.investment_tracker.auth.repository;

import com.thiru.investment_tracker.auth.entity.UserDetail;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserDetailsRepository extends MongoRepository<UserDetail, String> {

    Optional<UserDetail> findByEmail(String email);
}