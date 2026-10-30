package com.movieticket.show.service;

public class ShowIntegrationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ShowIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShowIntegrationException(String message) {
        super(message);
    }
}
