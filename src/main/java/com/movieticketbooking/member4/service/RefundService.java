package com.movieticketbooking.member4.service;

import com.movieticketbooking.member4.integration.BookingDetails;
import com.movieticketbooking.member4.integration.BookingServicePort;
import com.movieticketbooking.member4.integration.SeatServicePort;
import com.movieticketbooking.member4.model.Payment;
import com.movieticketbooking.member4.model.PaymentStatus;
import com.movieticketbooking.member4.model.RefundReason;
import com.movieticketbooking.member4.model.RefundTransaction;
import com.movieticketbooking.member4.repository.PaymentRepository;
import com.movieticketbooking.member4.repository.RefundRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class RefundService {
    private static final long CANCELLATION_CUTOFF_HOURS = 4;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final BookingServicePort bookingService;
    private final SeatServicePort seatService;
    private final Clock clock;

    public RefundService(PaymentRepository paymentRepository, RefundRepository refundRepository,
            BookingServicePort bookingService, SeatServicePort seatService, Clock clock) {
        this.paymentRepository = Objects.requireNonNull(paymentRepository);
        this.refundRepository = Objects.requireNonNull(refundRepository);
        this.bookingService = Objects.requireNonNull(bookingService);
        this.seatService = Objects.requireNonNull(seatService);
        this.clock = Objects.requireNonNull(clock);
    }

    public RefundTransaction cancelBooking(String bookingId) {
        BookingDetails booking = bookingService.getBookingDetails(bookingId);
        LocalDateTime now = LocalDateTime.now(clock);
        if (now.isAfter(booking.showStartTime().minusHours(CANCELLATION_CUTOFF_HOURS))) {
            throw new IllegalStateException("Bookings can only be cancelled at least 4 hours before the show");
        }
        Payment payment = findPaidPayment(bookingId)
                .orElseThrow(() -> new IllegalStateException("No paid payment exists for this booking"));
        BigDecimal ticketRefund = payment.getBreakdown().ticketAmount()
                .subtract(payment.getBreakdown().offerDiscount()).max(BigDecimal.ZERO);
        bookingService.cancelBooking(bookingId);
        seatService.releaseLockedSeats(bookingId);
        return createRefund(payment, ticketRefund, RefundReason.USER_CANCELLATION, now);
    }

    public List<RefundTransaction> cancelShow(String showId) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<BookingDetails> showBookings = bookingService.getBookingsForShow(showId);
        for (BookingDetails booking : showBookings) {
            Optional<Payment> paidPayment = findPaidPayment(booking.bookingId());
            bookingService.cancelBooking(booking.bookingId());
            seatService.releaseLockedSeats(booking.bookingId());
            paidPayment.ifPresent(payment -> createRefund(payment, payment.getAmount(),
                    RefundReason.SHOW_CANCELLATION, now));
        }
        return showBookings.stream()
                .map(booking -> refundRepository.findByBookingId(booking.bookingId()))
                .flatMap(List::stream)
                .filter(refund -> refund.reason() == RefundReason.SHOW_CANCELLATION)
                .toList();
    }

    public Optional<RefundTransaction> getRefundForPayment(String paymentId) {
        return refundRepository.findByPaymentId(paymentId);
    }

    public List<RefundTransaction> getRefundsForBooking(String bookingId) {
        return refundRepository.findByBookingId(bookingId);
    }

    private Optional<Payment> findPaidPayment(String bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.PAID).findFirst();
    }

    private RefundTransaction createRefund(Payment payment, BigDecimal amount, RefundReason reason,
            LocalDateTime now) {
        if (refundRepository.findByPaymentId(payment.getPaymentId()).isPresent()) {
            throw new IllegalStateException("A refund already exists for this payment");
        }
        RefundTransaction refund = new RefundTransaction(UUID.randomUUID().toString(), payment.getPaymentId(),
                payment.getBookingId(), amount, now, reason);
        refundRepository.save(refund);
        payment.markRefunded();
        return refund;
    }
}