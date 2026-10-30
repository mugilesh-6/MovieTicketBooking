package com.movieticketbooking.member4.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record BookingDetails(
        String bookingId,
        String userId,
        String showId,
        LocalDateTime showStartTime,
        List<BigDecimal> seatPrices) {
    public BookingDetails {
        Objects.requireNonNull(bookingId);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(showId);
        Objects.requireNonNull(showStartTime);
        seatPrices = List.copyOf(seatPrices);
        if (seatPrices.isEmpty() || seatPrices.stream().anyMatch(price -> price == null || price.signum() < 0)) {
            throw new IllegalArgumentException("Booking must contain non-negative seat prices");
        }
    }
}