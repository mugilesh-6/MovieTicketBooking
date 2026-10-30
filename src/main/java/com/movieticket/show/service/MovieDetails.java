package com.movieticket.show.service;

import java.time.LocalDate;
import java.util.Objects;

public final class MovieDetails {
    private final String id;
    private final String title;
    private final LocalDate releaseDate;
    private final int durationMinutes;

    public MovieDetails(String id, String title, LocalDate releaseDate, int durationMinutes) {
        this.id = requireText(id, "movie ID");
        this.title = requireText(title, "movie title");
        this.releaseDate = Objects.requireNonNull(releaseDate, "releaseDate");
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Movie duration must be positive");
        }
        this.durationMinutes = durationMinutes;
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

    public String getTitle() {
        return title;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
}
