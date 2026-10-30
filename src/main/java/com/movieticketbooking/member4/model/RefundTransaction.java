package com.movieticketbooking.member4.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record RefundTransaction(
        String refundId,
        String paymentId,
        String bookingId,
        BigDecimal amount,
        LocalDateTime refundDate,
        RefundReason reason) {
    public RefundTransaction {
        Objects.requireNonNull(refundId);
        Objects.requireNonNull(paymentId);
        Objects.requireNonNull(bookingId);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(refundDate);
        Objects.requireNonNull(reason);
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Refund amount cannot be negative");
        }
    }
}