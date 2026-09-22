package com.javaproject.authorization_decisioning.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private static final Logger logger =
        LoggerFactory.getLogger(JwtFilter.class);

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authorizationHeader.substring(7);

        try {

                String username =
                        jwtService.extractUsername(token);

                String role =
                        jwtService.extractRole(token);

                logger.info("JWT authenticated user: {}, role: {}", username, role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + role
                                )
                                )
                        );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
            logger.debug(
                "Authorities: {}",
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getAuthorities()
                );


        } catch (Exception exception) {

                logger.warn(
                        "JWT authentication failed: {}",
                        exception.getMessage()
                        );

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                return;
        }

        filterChain.doFilter(request, response);
    }
}