package com.ddaa.authservice.service;

import com.ddaa.authservice.dto.CreateUserRequest;
import com.ddaa.authservice.model.User;
import com.ddaa.authservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(CreateUserRequest request) {
        User user = new User();
        user.setGoogleId(request.getGoogleId());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        user.setActive(request.getActive() != null ? request.getActive() : true);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(null);

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createOrUpdateGoogleUser(String googleId, String name, String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();

            if (Boolean.FALSE.equals(user.getActive())) {
                throw new IllegalStateException("User is inactive");
            }

            user.setGoogleId(googleId);
            user.setName(name);
            user.setLastLogin(LocalDateTime.now());
        } else {
            user = new User();
            user.setGoogleId(googleId);
            user.setName(name);
            user.setEmail(email);
            user.setRole("USER");
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setLastLogin(LocalDateTime.now());
        }

        return userRepository.save(user);
    }
}