package com.movieticketbooking.member4;

import com.movieticketbooking.member4.integration.BookingDetails;
import com.movieticketbooking.member4.integration.BookingServicePort;
import com.movieticketbooking.member4.integration.SeatServicePort;
import com.movieticketbooking.member4.model.Offer;
import com.movieticketbooking.member4.model.Payment;
import com.movieticketbooking.member4.model.PaymentMethod;
import com.movieticketbooking.member4.model.PaymentStatus;
import com.movieticketbooking.member4.model.RefundReason;
import com.movieticketbooking.member4.repository.OfferRepository;
import com.movieticketbooking.member4.repository.PaymentRepository;
import com.movieticketbooking.member4.repository.RefundRepository;
import com.movieticketbooking.member4.service.OfferService;
import com.movieticketbooking.member4.service.PaymentService;
import com.movieticketbooking.member4.service.RefundService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class Member4ModuleTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 30, 12, 0);

    public static void main(String[] args) {
        successfulPaymentCalculatesAndConfirms();
        failedPaymentReleasesSeats();
        userRefundExcludesFeesAndPreventsDuplicates();
        showCancellationRefundsFullPaidAmount();
        offerIsCappedAndSingleUse();
        System.out.println("All Member 4 tests passed");
    }

    private static void successfulPaymentCalculatesAndConfirms() {
        Fixture fixture = new Fixture(true);
        Payment payment = fixture.pay("B1", null);
        equal(PaymentStatus.PAID, payment.getStatus());
        equal(new BigDecimal("290.80"), payment.getAmount());
        equal(1, fixture.booking.confirmed);
        equal(0, fixture.seats.released);
    }

    private static void failedPaymentReleasesSeats() {
        Fixture fixture = new Fixture(false);
        Payment payment = fixture.pay("B1", null);
        equal(PaymentStatus.FAILED, payment.getStatus());
        equal(1, fixture.booking.failed);
        equal(1, fixture.seats.released);
    }

    private static void userRefundExcludesFeesAndPreventsDuplicates() {
        Fixture fixture = new Fixture(true);
        Payment payment = fixture.pay("B1", null);
        var refund = fixture.refunds.cancelBooking("B1");
        equal(new BigDecimal("220.00"), refund.amount());
        equal(PaymentStatus.REFUNDED, payment.getStatus());
        equal(1, fixture.seats.released);
        expectFailure(() -> fixture.refunds.cancelBooking("B1"));
    }

    private static void showCancellationRefundsFullPaidAmount() {
        Fixture fixture = new Fixture(true);
        Payment payment = fixture.pay("B1", null);
        var refund = fixture.refunds.cancelShow("S1").get(0);
        equal(payment.getAmount(), refund.amount());
        equal(RefundReason.SHOW_CANCELLATION, refund.reason());
        equal(PaymentStatus.REFUNDED, payment.getStatus());
    }

    private static void offerIsCappedAndSingleUse() {
        Fixture fixture = new Fixture(true);
        fixture.offers.createOffer(new Offer("SAVE", new BigDecimal("50"), new BigDecimal("10"),
                NOW.minusDays(1), NOW.plusDays(1), BigDecimal.ZERO));
        Payment payment = fixture.pay("B1", "SAVE");
        equal(new BigDecimal("10.00"), payment.getBreakdown().offerDiscount());
        expectFailure(() -> fixture.pay("B2", "SAVE"));
    }

    private static void expectFailure(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected operation to fail");
        } catch (IllegalArgumentException | IllegalStateException expected) {
            // Expected validation failure.
        }
    }

    private static void equal(Object expected, Object actual) {
        if (!expected.equals(actual)) throw new AssertionError("Expected " + expected + ", got " + actual);
    }

    private static final class Fixture {
        final FakeBookingService booking = new FakeBookingService();
        final FakeSeatService seats = new FakeSeatService();
        final OfferService offers = new OfferService(new OfferRepository());
        final PaymentRepository payments = new PaymentRepository();
        final RefundService refunds;
        final PaymentService paymentService;

        Fixture(boolean succeeds) {
            Clock clock = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC);
            refunds = new RefundService(payments, new RefundRepository(), booking, seats, clock);
            paymentService = new PaymentService(payments, offers, booking, seats,
                    (method, amount) -> succeeds, clock);
        }

        Payment pay(String bookingId, String offer) {
            return paymentService.processPayment(bookingId, PaymentMethod.UPI, offer);
        }
    }

    private static final class FakeBookingService implements BookingServicePort {
        private final List<BookingDetails> bookings = new ArrayList<>(List.of(
                new BookingDetails("B1", "U1", "S1", NOW.plusDays(1),
                        List.of(new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("20"))),
                new BookingDetails("B2", "U1", "S1", NOW.plusDays(1), List.of(new BigDecimal("100")))));
        int confirmed;
        int failed;

        public BookingDetails getBookingDetails(String bookingId) {
            return bookings.stream().filter(booking -> booking.bookingId().equals(bookingId)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        }
        public void confirmBooking(String bookingId) { confirmed++; }
        public void markBookingFailed(String bookingId) { failed++; }
        public void cancelBooking(String bookingId) { }
        public List<BookingDetails> getBookingsForShow(String showId) {
            return bookings.stream().filter(booking -> booking.showId().equals(showId)).toList();
        }
    }

    private static final class FakeSeatService implements SeatServicePort {
        int released;
        public void releaseLockedSeats(String bookingId) { released++; }
    }
}