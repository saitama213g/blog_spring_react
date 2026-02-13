package com.myblog.blog.services.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import com.myblog.blog.domain.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.myblog.blog.services.AuthenticationService;
import com.myblog.blog.services.UserService;

import java.nio.file.AccessDeniedException;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService; // or add a method in here 
    private final UserService userService; // should i use this inside 

    @Value("${jwt.secret}")
    private String secretKey;

    private final Long jwtExpiryMs = 2 * 60 * 1000L; // 2 minutes in ms
    private final Long refreshExpiryMs = 7 * 24 * 60 * 60 * 1000L; // 7 days in ms

    @Override
    public UserDetails authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        return userDetailsService.loadUserByUsername(email);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiryMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiryMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public UserDetails validateToken(String token) {
        Claims claims = extractInfo(token);
        if (!"access".equals(claims.get("type")))
            throw new BadCredentialsException("misuse of refresh token");
        String username = claims.getSubject();
        return userDetailsService.loadUserByUsername(username);
    }

    @Override
    public UserDetails validateRefreshToken(String token) {
        Claims claims = extractInfo(token);
        // validate that the token is in the database
        if (!"refresh".equals(claims.get("type")))
            throw new BadCredentialsException("misuse of access token");
        User user = userService.findByJwtRefreshToken(token);
        return userDetailsService.loadUserByUsername(user.getEmail());
    }

    private Claims extractInfo(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }

    @Override
    public long getJwtExpirySeconds() {
        return jwtExpiryMs / 1000;
    }

    @Override
    public long getJwtRefreshExpirySeconds() {
        return refreshExpiryMs / 1000;
    }

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
