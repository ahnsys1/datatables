package com.angular.backend.users;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserMongoService {

    private final UserMongoRepository userRepository;

    public UserMongoService(UserMongoRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Flux<UserMongo> getAllUsers() {
        return userRepository.findAll();
    }

    public Mono<UserMongo> createUser(UserMongo user) {
        return userRepository.save(user);
    }

    public Mono<UserMongo> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Mono<UserMongo> updateUser(String id, UserMongo user) {
        return userRepository.findById(id)
                .flatMap(existingUser -> {
                    existingUser.setFirstName(user.getFirstName());
                    existingUser.setLastName(user.getLastName());
                    existingUser.setUsername(user.getUsername());
                    existingUser.setAge(user.getAge());
                    existingUser.setEmail(user.getEmail());
                    return userRepository.save(existingUser);
                });
    }

    public Mono<Void> deleteUser(String id) {
        return userRepository.deleteById(id);
    }
}