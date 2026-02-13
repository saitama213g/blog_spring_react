package com.myblog.blog.services;

import java.util.UUID;

import com.myblog.blog.domain.entities.User;
// import com.myblog.blog.domain.dtos.SiginRequest;

public interface UserService {
    User getUserById(UUID id);
    User registerUser(String name, String email, String passsword);
    public void setRefreshToken(String email, String refreshToken);
}
