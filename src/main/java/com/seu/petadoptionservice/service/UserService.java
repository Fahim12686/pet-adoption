package com.seu.petadoptionservice.service;

import com.seu.petadoptionservice.entity.User;
import com.seu.petadoptionservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of("ROLE_USER"));
        user.setStatus("ACTIVE");
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void updateUserStatus(Long id, String status) {
        userRepository.findById(id).ifPresent(user -> {
            user.setStatus(status);
            userRepository.save(user);
        });
    }

    public void promoteToStaff(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.getRoles().add("ROLE_STAFF");
            userRepository.save(user);
        });
    }
}