package com.myblog.blog.controllers;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myblog.blog.domain.dtos.AuthResponse;
import com.myblog.blog.domain.dtos.LoginRequest;
import com.myblog.blog.domain.dtos.SiginRequest;
import com.myblog.blog.services.AuthenticationService;
import com.myblog.blog.services.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {
        UserDetails userDetails = authenticationService.authenticate(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );
        String accessToken = authenticationService.generateToken(userDetails);
        String refreshToken = authenticationService.generateRefreshToken(userDetails);

        userservice.setRefreshToken(userDetails.getUsername(), refreshToken);

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) authenticationService.getJwtRefreshExpirySeconds());
        response.addCookie(refreshCookie);

        long nowSeconds = System.currentTimeMillis() / 1000L;
        long expiresAt = nowSeconds + authenticationService.getJwtExpirySeconds();

        AuthResponse authResponse = AuthResponse.builder()
        .token(accessToken)
        .expiresIn(authenticationService.getJwtExpirySeconds())
        .expiresAt(expiresAt)
        .build();
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid SiginRequest signinRequest, HttpServletResponse response) {
        User user = userservice.registerUser(
                signinRequest.getName(),
                signinRequest.getEmail(),
                signinRequest.getPassword()
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String refreshToken = authenticationService.generateRefreshToken(userDetails);
        userservice.setRefreshToken(userDetails.getUsername(), refreshToken);
        String token = authenticationService.generateToken(userDetails);
        
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) authenticationService.getJwtRefreshExpirySeconds());
        response.addCookie(refreshCookie);

        long nowSeconds = System.currentTimeMillis() / 1000L;
        long expiresAt = nowSeconds + authenticationService.getJwtExpirySeconds();

        AuthResponse authResponse = AuthResponse.builder()
        .token(token)
        .expiresIn(authenticationService.getJwtExpirySeconds())
        .expiresAt(expiresAt)
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
    public ResponseEntity<AuthResponse> refreshToken(@CookieValue(name="refresh_token") String refreshToken) {
        UserDetails userDetails = authenticationService.validateRefreshToken(refreshToken);
        String newToken = authenticationService.generateToken(userDetails);
                long nowSeconds = System.currentTimeMillis() / 1000L;
        long expiresAt = nowSeconds + authenticationService.getJwtExpirySeconds();

        AuthResponse authResponse = AuthResponse.builder()
        .token(newToken)
        .expiresIn(authenticationService.getJwtExpirySeconds())
        .expiresAt(expiresAt)
        .build();
        return ResponseEntity.ok(
            authResponse
        );
    }
}
