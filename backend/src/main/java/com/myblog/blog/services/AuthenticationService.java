package com.myblog.blog.services;

import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {
    UserDetails authenticate(String email, String password);
    String generateToken(UserDetails userDetails);
    String generateRefreshToken(UserDetails userDetails);
    UserDetails validateToken(String token);
    UserDetails validateRefreshToken(String token);
    long getJwtExpirySeconds();
    long getJwtRefreshExpirySeconds();
}
