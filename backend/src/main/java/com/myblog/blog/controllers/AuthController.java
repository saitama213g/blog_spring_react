package com.myblog.blog.controllers;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myblog.blog.domain.dtos.AuthResponse;
import com.myblog.blog.domain.dtos.LoginRequest;
import com.myblog.blog.domain.dtos.SiginRequest;
import com.myblog.blog.services.AuthenticationService;
import com.myblog.blog.services.UserService;
import com.myblog.blog.services.impl.TokenBlacklistService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

import com.myblog.blog.domain.entities.User;
// import com.myblog.blog.security.BlogUserDetails;
@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserDetailsService userDetailsService;
    private final UserService userservice;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        UserDetails userDetails = authenticationService.authenticate(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );
        String token = authenticationService.generateToken(userDetails);
        String refreshtoken = authenticationService.generateRefreshToken(userDetails);

        userservice.setRefreshToken(userDetails.getUsername(), refreshtoken);
        AuthResponse authResponse = AuthResponse.builder()
        .token(token)
        .expiresIn(authenticationService.getJwtExpirySeconds())
        .refresh_token(refreshtoken)
        .refresh_expiresIn(authenticationService.getJwtRefreshExpirySeconds())
        .build();
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid SiginRequest signinRequest) {
        User user = userservice.registerUser(
                signinRequest.getName(),
                signinRequest.getEmail(),
                signinRequest.getPassword()
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String refreshtoken = authenticationService.generateRefreshToken(userDetails);
        userservice.setRefreshToken(userDetails.getUsername(), refreshtoken);
        String token = authenticationService.generateToken(userDetails);

        AuthResponse authResponse = AuthResponse.builder()
        .token(token)
        .expiresIn(authenticationService.getJwtExpirySeconds())
        .refresh_token(refreshtoken)
        .refresh_expiresIn(authenticationService.getJwtRefreshExpirySeconds())
        .build();
        return ResponseEntity.ok(authResponse);
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        userservice.setRefreshToken(authentication.getName(), null);

        return ResponseEntity.ok("You Have Log Out Successfuly");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails)authentication.getPrincipal();
        String newtoken = authenticationService.generateToken(userDetails);
        Map<String, String> response = new HashMap<>();
        response.put("token", newtoken);
        return ResponseEntity.ok(response);
    }
    
    // maybe add another route for refresh-token that will be hit when the user refreshes or the token has timed out
}
