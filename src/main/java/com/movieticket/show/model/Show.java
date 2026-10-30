package com.movieticket.show.model;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Show {
    private final String id;
    private final String movieId;
    private final String theatreId;
    private final String screenId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final ShowPrices prices;
    private final ShowStatus status;

    public Show(String id, String movieId, String theatreId, String screenId,
                LocalDateTime startTime, LocalDateTime endTime, ShowPrices prices,
                ShowStatus status) {
        this.id = requireText(id, "show ID");
        this.movieId = requireText(movieId, "movie ID");
        this.theatreId = requireText(theatreId, "theatre ID");
        this.screenId = requireText(screenId, "screen ID");
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.endTime = Objects.requireNonNull(endTime, "endTime");
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Show end time must be after start time");
        }
        this.prices = Objects.requireNonNull(prices, "prices");
        this.status = Objects.requireNonNull(status, "status");
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value.trim();
    }

    public String getId() {
        return id;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getTheatreId() {
        return theatreId;
    }

    public String getScreenId() {
        return screenId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public ShowPrices getPrices() {
        return prices;
    }

    public ShowStatus getStatus() {
        return status;
    }

    public Show withPrices(ShowPrices updatedPrices) {
        return new Show(id, movieId, theatreId, screenId, startTime, endTime,
                updatedPrices, status);
    }

    public Show withStatus(ShowStatus updatedStatus) {
        return new Show(id, movieId, theatreId, screenId, startTime, endTime,
                prices, updatedStatus);
    }
}
