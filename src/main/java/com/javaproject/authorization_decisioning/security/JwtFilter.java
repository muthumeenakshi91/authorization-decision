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

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

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

                System.out.println("JWT username = " + username);
                System.out.println("JWT role = " + role);

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
            System.out.println(
                "Authenticated user = "
                        + SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName()
                );

                System.out.println(
                "Authorities = "
                        + SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getAuthorities()
                );

        } catch (Exception exception) {

                System.out.println(
                        "Exception in JwtFilter: "
                        + exception.getClass().getName()
                );

                System.out.println(
                        "Exception message: "
                        + exception.getMessage()
                );

                exception.printStackTrace();

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                return;
        }

        filterChain.doFilter(request, response);
    }
}