package com.movieticketbooking.member4.integration;

import java.util.List;

public interface BookingServicePort {
    BookingDetails getBookingDetails(String bookingId);
    void confirmBooking(String bookingId);
    void markBookingFailed(String bookingId);
    void cancelBooking(String bookingId);
    List<BookingDetails> getBookingsForShow(String showId);
}