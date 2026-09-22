package com.javaproject.authorization_decisioning.dto;

public class AuthorizationResponse {

    private String transactionId;
    private String decision;
    private String reason;
    private long processingTime;

    public AuthorizationResponse(String transactionId,
                                 String decision,
                                 String reason,
                                 long processingTime) {
        this.transactionId = transactionId;
        this.decision = decision;
        this.reason = reason;
        this.processingTime = processingTime;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getDecision() {
        return decision;
    }

    public String getReason() {
        return reason;
    }

    public long getProcessingTime() {
        return processingTime;
    }
}