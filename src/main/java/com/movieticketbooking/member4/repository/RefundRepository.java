package com.movieticketbooking.member4.repository;

import com.movieticketbooking.member4.model.RefundTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RefundRepository {
    private final List<RefundTransaction> refunds = new ArrayList<>();

    public synchronized void save(RefundTransaction refund) {
        if (findByPaymentId(refund.paymentId()).isPresent()) {
            throw new IllegalStateException("A refund already exists for this payment");
        }
        refunds.add(refund);
    }

    public synchronized Optional<RefundTransaction> findByPaymentId(String paymentId) {
        return refunds.stream().filter(refund -> refund.paymentId().equals(paymentId)).findFirst();
    }

    public synchronized List<RefundTransaction> findByBookingId(String bookingId) {
        return refunds.stream().filter(refund -> refund.bookingId().equals(bookingId)).toList();
    }
}