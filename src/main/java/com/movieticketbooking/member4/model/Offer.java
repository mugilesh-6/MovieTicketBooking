package com.movieticketbooking.member4.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record Offer(
        String code,
        BigDecimal discountPercentage,
        BigDecimal maximumDiscount,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        BigDecimal minimumBookingAmount) {
    public Offer {
        Objects.requireNonNull(code);
        Objects.requireNonNull(discountPercentage);
        Objects.requireNonNull(maximumDiscount);
        Objects.requireNonNull(validFrom);
        Objects.requireNonNull(validUntil);
        Objects.requireNonNull(minimumBookingAmount);
        if (code.isBlank() || discountPercentage.signum() <= 0
                || discountPercentage.compareTo(new BigDecimal("100")) > 0
                || maximumDiscount.signum() < 0 || minimumBookingAmount.signum() < 0
                || validUntil.isBefore(validFrom)) {
            throw new IllegalArgumentException("Invalid offer details");
        }
    }
}