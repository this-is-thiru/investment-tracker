package com.thiru.investment_tracker.repository.test;

import com.thiru.investment_tracker.entity.test.EntityTest1;
import com.thiru.investment_tracker.entity.test.EntityTest2;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.mongodb.repository.MongoRepository;

@JaversSpringDataAuditable
public interface EntityTest2Repository extends MongoRepository<EntityTest2, String> {

}
