package com.movieticketbooking.member4.service;

import com.movieticketbooking.member4.model.PaymentMethod;
import java.math.BigDecimal;

@FunctionalInterface
public interface PaymentSimulator {
    boolean process(PaymentMethod method, BigDecimal amount);
}