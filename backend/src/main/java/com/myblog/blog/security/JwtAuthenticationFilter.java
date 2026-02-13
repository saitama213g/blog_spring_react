package com.myblog.blog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.myblog.blog.services.AuthenticationService;
import com.myblog.blog.services.impl.TokenBlacklistService;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;
    // private final TokenBlacklistService tokenBlacklistService;

    private static final List<RequestMatcher> PUBLIC_ENDPOINTS = List.of(
            new AntPathRequestMatcher("/api/v1/auth/login", "POST"),
            new AntPathRequestMatcher("/api/v1/auth/register", "POST"),
            new AntPathRequestMatcher("/api/v1/categories/**", "GET"),
            new AntPathRequestMatcher("/api/v1/tags/**", "GET")
    );

    // private static final RequestMatcher REFRESH_PATH = new AntPathRequestMatcher("/api/v1/auth/refresh-token", "POST");

    private boolean isPublic(HttpServletRequest request) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(matcher -> matcher.matches(request));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            
            if (token == null || isPublic(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = authenticationService.validateToken(token);

                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                token,
                                userDetails.getAuthorities()
                        );
    
                SecurityContextHolder.getContext().setAuthentication(authentication);

                if (userDetails instanceof BlogUserDetails) {
                    request.setAttribute("userId", ((BlogUserDetails) userDetails).getId());
                    request.setAttribute("email", ((BlogUserDetails) userDetails).getUsername());
                    request.setAttribute("name", ((BlogUserDetails) userDetails).getName());
                }
            }

        } catch (Exception ex) {
            // Clear the SecurityContext so no fake authentication stays
            SecurityContextHolder.clearContext();

            // Log the issue
            log.warn("Invalid auth token: {}", ex.getMessage());

            // Respond with 401 Unauthorized and custom message
            try {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"error\": \"" + ex.getMessage() + "\"}");
                response.getWriter().flush();
            } catch (IOException ioEx) {
                log.error("Failed to write response", ioEx);
            }

            return; // stop further filter processing
        }

    
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
