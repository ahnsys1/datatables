package com.angular.backend.users;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMongoRepository extends ReactiveMongoRepository<UserMongo, String> {
}