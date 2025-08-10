package com.angular.backend.users;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserMongoService {

    @Autowired
    private UserMongoRepository userMongoRepository;

    public UserMongo createUser(UserMongo user) {
        return userMongoRepository.save(user);
    }

    public List<UserMongo> getAllUsers() {
        return userMongoRepository.findAll();
    }

    public UserMongo getUserById(String id) {
        return userMongoRepository.findById(id).orElse(null);
    }

    public UserMongo updateUser(String id, UserMongo user) {
        if (!userMongoRepository.existsById(id)) {
            return null;
        }
        user.setId(id);
        return userMongoRepository.save(user);
    }

    public boolean deleteUser(String id) {
        if (!userMongoRepository.existsById(id)) {
            return false;
        }
        userMongoRepository.deleteById(id);
        return true;
    }
}
