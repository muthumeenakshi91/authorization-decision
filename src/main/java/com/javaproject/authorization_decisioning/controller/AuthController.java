package com.javaproject.authorization_decisioning.controller;

import com.javaproject.authorization_decisioning.dto.LoginRequest;
import com.javaproject.authorization_decisioning.security.JwtService;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/login")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping
        public Map<String, String> login(
                @RequestBody LoginRequest request) {

        if ("admin".equals(request.username())
                && "password".equals(request.password())) {

                return Map.of(
                "token",
                jwtService.generateToken(
                        request.username(),
                        "ADMIN"
                )
                );
        }

        if ("user".equals(request.username())
                && "password".equals(request.password())) {

                return Map.of(
                "token",
                jwtService.generateToken(
                        request.username(),
                        "USER"
                )
                );
        }

        throw new RuntimeException("Invalid credentials");
    }
}