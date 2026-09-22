package com.javaproject.authorization_decisioning.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class AuthorizationRequest {

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
    @NotBlank(message = "Card Number is required")
    @Pattern(
        regexp = "\\d{16}",
        message = "Card number must contain exactly 16 digits"
    )
    private String cardNumber;
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private Double amount;
    @NotBlank(message = "Currency is required")
    private String currency;
    @NotBlank(message = "Merchant ID is required")
    private String merchant;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }
}