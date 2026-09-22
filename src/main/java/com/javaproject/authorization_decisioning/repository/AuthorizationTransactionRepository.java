package com.javaproject.authorization_decisioning.repository;

import com.javaproject.authorization_decisioning.entity.AuthorizationTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorizationTransactionRepository
        extends JpaRepository<AuthorizationTransaction, Long> {

    Optional<AuthorizationTransaction> findByTransactionId(
            String transactionId);
}