package com.angular.backend.users;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public UserJPA createUser(UserJPA user) {
        user.setId(null);
        UserJPA savedUser = userRepository.save(user);
        return savedUser;
    }

    @Transactional
    public List<UserJPA> getAllUsers() {
        List<UserJPA> allUsers = userRepository.findAll();
        return allUsers;
    }

    @Transactional
    public UserJPA getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public UserJPA updateUser(String id, UserJPA user) {
        if (!userRepository.existsById(id)) {
            return null;
        }
        user.setId(id);
        return userRepository.save(user);
    }

    @Transactional
    public boolean deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }
}
