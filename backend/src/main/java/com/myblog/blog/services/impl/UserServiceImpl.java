package com.myblog.blog.services.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.myblog.blog.domain.entities.User;
import com.myblog.blog.repositories.UserRepository;
import com.myblog.blog.services.UserService;
import java.lang.IllegalStateException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserById(UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Override
    public User registerUser(String name, String email, String passsword)
    {
        if (userRepository.findByEmail(email).isPresent())
            throw new IllegalStateException("Email already in use");
        User usr = new User();
        usr.setName(name);
        usr.setEmail(email);
        usr.setPassword(passwordEncoder.encode(passsword));
        return userRepository.save(usr);
    }
    @Transactional
    @Override
    public void setRefreshToken(String email, String refreshToken) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setJwtRefreshToken(refreshToken);
    }
}
