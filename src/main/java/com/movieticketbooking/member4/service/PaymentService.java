package com.movieticketbooking.member4.service;

import com.movieticketbooking.member4.integration.BookingDetails;
import com.movieticketbooking.member4.integration.BookingServicePort;
import com.movieticketbooking.member4.integration.SeatServicePort;
import com.movieticketbooking.member4.model.Offer;
import com.movieticketbooking.member4.model.Payment;
import com.movieticketbooking.member4.model.PaymentBreakdown;
import com.movieticketbooking.member4.model.PaymentMethod;
import com.movieticketbooking.member4.model.PaymentStatus;
import com.movieticketbooking.member4.repository.PaymentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PaymentService {
    private static final BigDecimal FEE_PER_TICKET = new BigDecimal("20.00");
    private static final BigDecimal GST_RATE = new BigDecimal("0.18");
    private final PaymentRepository paymentRepository;
    private final OfferService offerService;
    private final BookingServicePort bookingService;
    private final SeatServicePort seatService;
    private final PaymentSimulator paymentSimulator;
    private final Clock clock;

    public PaymentService(PaymentRepository paymentRepository, OfferService offerService,
            BookingServicePort bookingService, SeatServicePort seatService,
            PaymentSimulator paymentSimulator, Clock clock) {
        this.paymentRepository = Objects.requireNonNull(paymentRepository);
        this.offerService = Objects.requireNonNull(offerService);
        this.bookingService = Objects.requireNonNull(bookingService);
        this.seatService = Objects.requireNonNull(seatService);
        this.paymentSimulator = Objects.requireNonNull(paymentSimulator);
        this.clock = Objects.requireNonNull(clock);
    }

    public PaymentBreakdown calculateAmount(List<BigDecimal> seatPrices, String userId, String offerCode) {
        if (seatPrices == null || seatPrices.isEmpty()) {
            throw new IllegalArgumentException("At least one seat is required");
        }
        BigDecimal ticketAmount = BigDecimal.ZERO;
        for (BigDecimal price : seatPrices) {
            if (price == null || price.signum() < 0) throw new IllegalArgumentException("Invalid seat price");
            ticketAmount = ticketAmount.add(price);
        }
        ticketAmount = ticketAmount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal convenienceFee = FEE_PER_TICKET.multiply(BigDecimal.valueOf(seatPrices.size()));
        BigDecimal gst = convenienceFee.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal beforeDiscount = ticketAmount.add(convenienceFee).add(gst);
        BigDecimal discount = BigDecimal.ZERO.setScale(2);
        if (offerCode != null && !offerCode.isBlank()) {
            Offer offer = offerService.validateOffer(offerCode, userId, beforeDiscount, LocalDateTime.now(clock));
            discount = offerService.calculateDiscount(offer, beforeDiscount);
        }
        return new PaymentBreakdown(ticketAmount, convenienceFee, gst, discount,
                beforeDiscount.subtract(discount).setScale(2, RoundingMode.HALF_UP));
    }

    public Payment processPayment(String bookingId, PaymentMethod method, String offerCode) {
        Objects.requireNonNull(method, "Payment method is required");
        if (!paymentRepository.findByBookingId(bookingId).isEmpty()) {
            throw new IllegalStateException("A payment attempt already exists for this booking");
        }
        BookingDetails booking = bookingService.getBookingDetails(bookingId);
        PaymentBreakdown breakdown = calculateAmount(booking.seatPrices(), booking.userId(), offerCode);
        boolean successful = paymentSimulator.process(method, breakdown.totalAmount());
        Payment payment = new Payment(UUID.randomUUID().toString(), bookingId, breakdown.totalAmount(),
                LocalDateTime.now(clock), method,
                successful ? PaymentStatus.PAID : PaymentStatus.FAILED, breakdown);
        paymentRepository.save(payment);
        if (!successful) {
            bookingService.markBookingFailed(bookingId);
            seatService.releaseLockedSeats(bookingId);
            return payment;
        }
        if (offerCode != null && !offerCode.isBlank()) offerService.markUsed(booking.userId(), offerCode.trim());
        bookingService.confirmBooking(bookingId);
        return payment;
    }

    public Payment getPayment(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
    }

    public List<Payment> getPaymentsForBooking(String bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    public PaymentStatus getPaymentStatus(String paymentId) {
        return getPayment(paymentId).getStatus();
    }

}