package com.movieticketbooking.member4.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Payment {
    private final String paymentId;
    private final String bookingId;
    private final BigDecimal amount;
    private final LocalDateTime paymentDate;
    private final PaymentMethod paymentMethod;
    private PaymentStatus status;
    private final PaymentBreakdown breakdown;

    public Payment(String paymentId, String bookingId, BigDecimal amount, LocalDateTime paymentDate,
            PaymentMethod paymentMethod, PaymentStatus status, PaymentBreakdown breakdown) {
        this.paymentId = Objects.requireNonNull(paymentId);
        this.bookingId = Objects.requireNonNull(bookingId);
        this.amount = Objects.requireNonNull(amount);
        this.paymentDate = Objects.requireNonNull(paymentDate);
        this.paymentMethod = Objects.requireNonNull(paymentMethod);
        this.status = Objects.requireNonNull(status);
        this.breakdown = Objects.requireNonNull(breakdown);
    }

    public String getPaymentId() { return paymentId; }
    public String getBookingId() { return bookingId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getStatus() { return status; }
    public PaymentBreakdown getBreakdown() { return breakdown; }

    @Override
    public String toString() {
        return "Payment{" + paymentId + ", booking=" + bookingId + ", amount=" + amount
                + ", method=" + paymentMethod + ", status=" + status + ", date=" + paymentDate + '}';
    }

    public void markRefunded() {
        if (status != PaymentStatus.PAID) {
            throw new IllegalStateException("Only paid payments can be refunded");
        }
        status = PaymentStatus.REFUNDED;
    }
}