package com.movieticket.show.service;

public interface RefundGateway {
    void refundAllForShow(String showId) throws ShowIntegrationException;
}
