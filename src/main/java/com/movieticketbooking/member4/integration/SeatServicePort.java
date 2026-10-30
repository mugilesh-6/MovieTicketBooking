package com.movieticketbooking.member4.integration;

public interface SeatServicePort {
    void releaseLockedSeats(String bookingId);
}