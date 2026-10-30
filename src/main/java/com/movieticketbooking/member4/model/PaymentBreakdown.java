package com.movieticketbooking.member4.model;

import java.math.BigDecimal;

public record PaymentBreakdown(
        BigDecimal ticketAmount,
        BigDecimal convenienceFee,
        BigDecimal gst,
        BigDecimal offerDiscount,
        BigDecimal totalAmount) {
}