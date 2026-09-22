package com.javaproject.authorization_decisioning.service;

import com.javaproject.authorization_decisioning.dto.AuthorizationRequest;
import com.javaproject.authorization_decisioning.dto.AuthorizationResponse;
import com.javaproject.authorization_decisioning.entity.AuthorizationTransaction;
import com.javaproject.authorization_decisioning.repository.AuthorizationTransactionRepository;
import com.javaproject.authorization_decisioning.exception.TransactionNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthorizationService {

    private final AuthorizationTransactionRepository repository;
    
    private static final Logger logger =
        LoggerFactory.getLogger(AuthorizationService.class);

    public AuthorizationService(
            AuthorizationTransactionRepository repository) {
        this.repository = repository;
    }

    public AuthorizationResponse authorize(
            AuthorizationRequest request) {

        logger.info(
                "Authorization request received. transactionId={}, amount={}, currency={}",
                request.getTransactionId(),
                request.getAmount(),
                request.getCurrency()
        );
        long startTime = System.currentTimeMillis();

        if (repository.findByTransactionId(request.getTransactionId()).isPresent()) {
                logger.warn(
                        "Duplicate transaction detected. transactionId={}",
                        request.getTransactionId()
                );
                long processingTime =
                        System.currentTimeMillis() - startTime;
                return new AuthorizationResponse(
                        request.getTransactionId(),
                        "DECLINED",
                        "Duplicate transaction",
                        processingTime
                );
        }

        if (!isValidCurrency(request.getCurrency())) {
                logger.warn(
                        "Unsupported currency. transactionId={}, currency={}",
                        request.getTransactionId(),
                        request.getCurrency()
                );
                long processingTime =
                        System.currentTimeMillis() - startTime;

                return new AuthorizationResponse(
                        request.getTransactionId(),
                        "DECLINED",
                        "Unsupported currency",
                        processingTime
                );
        }
        String decision;
        String reason;

        if (request.getAmount() > 5000) {
            decision = "DECLINED";
            reason = "Transaction amount exceeds authorization limit";
        } else {
            decision = "APPROVED";
            reason = "Transaction approved";
        }
        logger.info(
                "Authorization decision. transactionId={}, decision={}, reason={}",
                request.getTransactionId(),
                decision,
                reason
        );
        long processingTime =
                System.currentTimeMillis() - startTime;

        AuthorizationTransaction transaction =
                new AuthorizationTransaction();

        transaction.setTransactionId(request.getTransactionId());
        transaction.setCardNumber(request.getCardNumber());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setMerchant(request.getMerchant());
        transaction.setDecision(decision);
        transaction.setReason(reason);
        transaction.setProcessingTime(processingTime);

        repository.save(transaction);
        logger.info(
                "Authorization transaction saved. transactionId={}",
                request.getTransactionId()
        );
        return new AuthorizationResponse(
                request.getTransactionId(),
                decision,
                reason,
                processingTime
        );
    }

    public AuthorizationTransaction getTransaction(
            String transactionId) {
                
        logger.info(
                "Fetching authorization transaction. transactionId={}",
                transactionId
        );

        return repository.findByTransactionId(transactionId)
                .orElseThrow(() ->
                        new TransactionNotFoundException(
                                "Transaction not found: "
                                        + transactionId));
    }

    private boolean isValidCurrency(String currency) {

        return currency.equals("CAD")
                || currency.equals("USD")
                || currency.equals("EUR")
                || currency.equals("GBP");
    }
}