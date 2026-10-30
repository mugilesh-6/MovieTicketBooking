package com.movieticketbooking.member4.service;

import com.movieticketbooking.member4.model.PaymentMethod;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public class SimulatedPaymentProcessor implements PaymentSimulator {
    private final BooleanSupplier nextResult;

    public SimulatedPaymentProcessor(BooleanSupplier nextResult) {
        this.nextResult = Objects.requireNonNull(nextResult);
    }

    @Override
    public boolean process(PaymentMethod method, BigDecimal amount) {
        return nextResult.getAsBoolean();
    }
}