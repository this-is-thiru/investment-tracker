package com.thiru.wealthlens.auth.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.thiru.wealthlens.auth.entity.UserDetail;

public interface UserDetailsRepository extends MongoRepository<UserDetail, String> {

	Optional<UserDetail> findByEmail(String email);
}
