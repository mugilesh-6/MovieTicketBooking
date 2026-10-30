package com.movieticketbooking.member4.repository;

import com.movieticketbooking.member4.model.Payment;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentRepository {
    private final List<Payment> payments = new ArrayList<>();

    public synchronized void save(Payment payment) {
        if (findByPaymentId(payment.getPaymentId()).isPresent()) {
            throw new IllegalArgumentException("Payment ID already exists: " + payment.getPaymentId());
        }
        payments.add(payment);
    }

    public synchronized Optional<Payment> findByPaymentId(String paymentId) {
        return payments.stream().filter(payment -> payment.getPaymentId().equals(paymentId)).findFirst();
    }

    public synchronized List<Payment> findByBookingId(String bookingId) {
        return payments.stream().filter(payment -> payment.getBookingId().equals(bookingId)).toList();
    }
}