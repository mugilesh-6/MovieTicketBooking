package com.movieticket.show.service;

public interface SeatLockGateway {
    void invalidateLocksForShow(String showId) throws ShowIntegrationException;
}
