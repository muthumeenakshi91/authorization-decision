package com.javaproject.authorization_decisioning.service;

import com.javaproject.authorization_decisioning.dto.AuthorizationRequest;
import com.javaproject.authorization_decisioning.dto.AuthorizationResponse;
import com.javaproject.authorization_decisioning.entity.AuthorizationTransaction;
import com.javaproject.authorization_decisioning.repository.AuthorizationTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.javaproject.authorization_decisioning.exception.TransactionNotFoundException;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private AuthorizationTransactionRepository repository;

    @InjectMocks
    private AuthorizationService authorizationService;

    @Test
    void shouldApproveValidTransaction() {

        // Arrange
        AuthorizationRequest request = new AuthorizationRequest();

        request.setTransactionId("TXN1001");
        request.setCardNumber("4111111111111111");
        request.setAmount(250.00);
        request.setCurrency("CAD");
        request.setMerchant("ABC_STORE");

        when(repository.findByTransactionId("TXN1001"))
                .thenReturn(Optional.empty());

        AuthorizationTransaction savedTransaction =
                new AuthorizationTransaction();

        when(repository.save(any(AuthorizationTransaction.class)))
                .thenReturn(savedTransaction);

        // Act
        AuthorizationResponse response =
                authorizationService.authorize(request);

        // Assert
        assertEquals("APPROVED", response.getDecision());
        assertEquals("Transaction approved", response.getReason());

        verify(repository).findByTransactionId("TXN1001");
        verify(repository).save(any(AuthorizationTransaction.class));
    }

    @Test
    void shouldDeclineTransactionWhenAmountExceedsLimit() {

        // Arrange
        AuthorizationRequest request = new AuthorizationRequest();

        request.setTransactionId("TXN1002");
        request.setCardNumber("4111111111111111");
        request.setAmount(6000.00);
        request.setCurrency("CAD");
        request.setMerchant("ABC_STORE");

        when(repository.findByTransactionId("TXN1002"))
                .thenReturn(Optional.empty());

        // Act
        AuthorizationResponse response =
                authorizationService.authorize(request);

        // Assert
        assertEquals("DECLINED", response.getDecision());
        assertEquals(
                "Transaction amount exceeds authorization limit",
                response.getReason()
        );

        verify(repository).findByTransactionId("TXN1002");
        verify(repository)
                .save(any(AuthorizationTransaction.class));
    }

    @Test
    void shouldDeclineDuplicateTransaction() {

        AuthorizationRequest request = new AuthorizationRequest();

        request.setTransactionId("TXN1001");
        request.setCardNumber("4111111111111111");
        request.setAmount(250.00);
        request.setCurrency("CAD");
        request.setMerchant("ABC_STORE");

        AuthorizationTransaction existingTransaction =
                new AuthorizationTransaction();

        when(repository.findByTransactionId("TXN1001"))
                .thenReturn(Optional.of(existingTransaction));

        AuthorizationResponse response =
                authorizationService.authorize(request);

        assertEquals("DECLINED", response.getDecision());
        assertEquals("Duplicate transaction", response.getReason());

        verify(repository).findByTransactionId("TXN1001");

        verify(repository, never())
                .save(any(AuthorizationTransaction.class));
    }

    @Test
    void unSupportedCurrency(){
        AuthorizationRequest request = new AuthorizationRequest();

        request.setTransactionId("TXN1001");
        request.setCardNumber("4111111111111111");
        request.setAmount(250.00);
        request.setCurrency("XYZ");
        request.setMerchant("ABC_STORE");

        when(repository.findByTransactionId(request.getTransactionId()))
                .thenReturn(Optional.empty());
        
        AuthorizationResponse response = 
                authorizationService.authorize(request);
        //AuthorizationResponse response =
                //authorizationService.authorize(request);

        assertEquals(response.getDecision(), "DECLINED");
        assertEquals(response.getReason(), "Unsupported currency");

        verify(repository).findByTransactionId(request.getTransactionId());
        verify(repository, never()).save(any(AuthorizationTransaction.class));
    }

    @Test
    void shouldReturnTransactionWhenFound() {

        AuthorizationTransaction transaction =
                new AuthorizationTransaction();

        transaction.setTransactionId("TXN1001");
        transaction.setCardNumber("4111111111111111");
        transaction.setAmount(250.00);
        transaction.setCurrency("CAD");
        transaction.setMerchant("ABC_STORE");
        transaction.setDecision("APPROVED");
        transaction.setReason("Transaction approved");
        transaction.setProcessingTime(45);

        when(repository.findByTransactionId("TXN1001"))
                .thenReturn(Optional.of(transaction));

        AuthorizationTransaction result =
                authorizationService.getTransaction("TXN1001");

        assertNotNull(result);
        assertEquals("TXN1001", result.getTransactionId());
        assertEquals("APPROVED", result.getDecision());
        assertEquals("Transaction approved", result.getReason());

        verify(repository).findByTransactionId("TXN1001");
    }

        
        @Test
        void shouldThrowExceptionWhenTransactionNotFound() {

        when(repository.findByTransactionId("TXN9999"))
                .thenReturn(Optional.empty());

        TransactionNotFoundException exception =
                assertThrows(
                        TransactionNotFoundException.class,
                        () -> authorizationService.getTransaction("TXN9999")
                );

        assertEquals(
                "Transaction not found: TXN9999",
                exception.getMessage()
        );

        verify(repository).findByTransactionId("TXN9999");

        verify(repository, never())
                .save(any(AuthorizationTransaction.class));
        }

        @Test
        void shouldSaveCorrectAuthorizationTransaction() {

                AuthorizationRequest request = new AuthorizationRequest();

                request.setTransactionId("TXN1004");
                request.setCardNumber("4111111111111111");
                request.setAmount(300.00);
                request.setCurrency("CAD");
                request.setMerchant("ABC_STORE");

                when(repository.findByTransactionId("TXN1004"))
                        .thenReturn(Optional.empty());

                AuthorizationTransaction savedTransaction =
                        new AuthorizationTransaction();

                when(repository.save(any(AuthorizationTransaction.class)))
                        .thenReturn(savedTransaction);

                authorizationService.authorize(request);

                ArgumentCaptor<AuthorizationTransaction> captor =
                        ArgumentCaptor.forClass(AuthorizationTransaction.class);

                verify(repository).save(captor.capture());

                AuthorizationTransaction transaction =
                        captor.getValue();

                assertEquals("TXN1004", transaction.getTransactionId());
                assertEquals("4111111111111111", transaction.getCardNumber());
                assertEquals(300.00, transaction.getAmount());
                assertEquals("CAD", transaction.getCurrency());
                assertEquals("ABC_STORE", transaction.getMerchant());
                assertEquals("APPROVED", transaction.getDecision());
                assertEquals("Transaction approved", transaction.getReason());
        }
}