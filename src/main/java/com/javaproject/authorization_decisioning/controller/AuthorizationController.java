package com.javaproject.authorization_decisioning.controller;

import com.javaproject.authorization_decisioning.dto.AuthorizationRequest;
import com.javaproject.authorization_decisioning.dto.AuthorizationResponse;
import com.javaproject.authorization_decisioning.service.AuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.javaproject.authorization_decisioning.entity.AuthorizationTransaction;

@RestController
@RequestMapping("/api/auth")
public class AuthorizationController {

    private final AuthorizationService authorizationService;

    public AuthorizationController(
            AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping("/authorize")
    public ResponseEntity<AuthorizationResponse> authorize(
            @Valid @RequestBody AuthorizationRequest request) {

        AuthorizationResponse response =
                authorizationService.authorize(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<AuthorizationTransaction> getTransaction(
            @PathVariable String transactionId) {

        AuthorizationTransaction transaction =
                authorizationService.getTransaction(transactionId);

        return ResponseEntity.ok(transaction);
    }
}