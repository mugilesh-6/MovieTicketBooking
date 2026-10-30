package com.movieticket.show.service;

public interface BookingGateway {
    boolean hasBookingsForShow(String showId);

    void cancelBookingsForShow(String showId) throws ShowIntegrationException;

    boolean theatreHasUpcomingBookings(String theatreId);
}
