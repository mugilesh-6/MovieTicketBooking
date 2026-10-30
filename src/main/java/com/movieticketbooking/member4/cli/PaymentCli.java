package com.movieticketbooking.member4.cli;

import com.movieticketbooking.member4.model.Offer;
import com.movieticketbooking.member4.model.Payment;
import com.movieticketbooking.member4.model.PaymentMethod;
import com.movieticketbooking.member4.model.RefundTransaction;
import com.movieticketbooking.member4.service.OfferService;
import com.movieticketbooking.member4.service.PaymentService;
import com.movieticketbooking.member4.service.RefundService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class PaymentCli {
    private final PaymentService paymentService;
    private final OfferService offerService;
    private final RefundService refundService;
    private final Scanner input;

    public PaymentCli(PaymentService paymentService, OfferService offerService,
            RefundService refundService, Scanner input) {
        this.paymentService = paymentService;
        this.offerService = offerService;
        this.refundService = refundService;
        this.input = input;
    }

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("\n1 Pay booking  2 Payment details  3 Booking history  4 Cancel booking");
            System.out.println("5 Cancel show (admin)  6 Create offer  0 Exit");
            switch (input.nextLine().trim()) {
                case "1" -> payBooking();
                case "2" -> showPayment();
                case "3" -> showHistory();
                case "4" -> cancelBooking();
                case "5" -> cancelShow();
                case "6" -> createOffer();
                case "0" -> running = false;
                default -> System.out.println("Invalid selection");
            }
        }
    }

    private void payBooking() {
        System.out.print("Booking ID: ");
        String bookingId = input.nextLine().trim();
        System.out.print("Method (UPI, CARD, NET_BANKING): ");
        PaymentMethod method = PaymentMethod.valueOf(input.nextLine().trim().toUpperCase());
        System.out.print("Offer code (blank for none): ");
        Payment payment = paymentService.processPayment(bookingId, method, input.nextLine().trim());
        System.out.println("Payment " + payment.getPaymentId() + " status: " + payment.getStatus()
                + ", amount: Rs. " + payment.getAmount());
    }

    private void showPayment() {
        System.out.print("Payment ID: ");
        Payment payment = paymentService.getPayment(input.nextLine().trim());
        System.out.println(payment.getPaymentId() + " | booking " + payment.getBookingId() + " | Rs. "
                + payment.getAmount() + " | " + payment.getPaymentMethod() + " | " + payment.getStatus());
        refundService.getRefundForPayment(payment.getPaymentId()).ifPresent(System.out::println);
    }

    private void showHistory() {
        System.out.print("Booking ID: ");
        String bookingId = input.nextLine().trim();
        paymentService.getPaymentsForBooking(bookingId).forEach(System.out::println);
        refundService.getRefundsForBooking(bookingId).forEach(System.out::println);
    }

    private void cancelBooking() {
        System.out.print("Booking ID: ");
        RefundTransaction refund = refundService.cancelBooking(input.nextLine().trim());
        System.out.println("Refund: Rs. " + refund.amount() + " (" + refund.reason() + ")");
    }

    private void cancelShow() {
        System.out.print("Show ID: ");
        List<RefundTransaction> refunds = refundService.cancelShow(input.nextLine().trim());
        System.out.println("Refunds issued: " + refunds.size());
    }

    private void createOffer() {
        System.out.print("Code: ");
        String code = input.nextLine().trim();
        System.out.print("Discount percentage: ");
        BigDecimal percentage = new BigDecimal(input.nextLine().trim());
        System.out.print("Maximum discount: ");
        BigDecimal maximum = new BigDecimal(input.nextLine().trim());
        System.out.print("Valid from (yyyy-MM-ddTHH:mm): ");
        LocalDateTime start = LocalDateTime.parse(input.nextLine().trim());
        System.out.print("Valid until (yyyy-MM-ddTHH:mm): ");
        LocalDateTime end = LocalDateTime.parse(input.nextLine().trim());
        System.out.print("Minimum booking amount: ");
        BigDecimal minimum = new BigDecimal(input.nextLine().trim());
        offerService.createOffer(new Offer(code, percentage, maximum, start, end, minimum));
        System.out.println("Offer created");
    }
}