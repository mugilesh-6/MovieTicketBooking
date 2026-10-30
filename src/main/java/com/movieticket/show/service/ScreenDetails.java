package com.movieticket.show.service;

import java.util.Objects;

public final class ScreenDetails {
    private final String theatreId;
    private final String theatreName;
    private final String screenId;
    private final String screenName;

    public ScreenDetails(String theatreId, String theatreName, String screenId, String screenName) {
        this.theatreId = requireText(theatreId, "theatre ID");
        this.theatreName = requireText(theatreName, "theatre name");
        this.screenId = requireText(screenId, "screen ID");
        this.screenName = requireText(screenName, "screen name");
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value.trim();
    }

    public String getTheatreId() {
        return theatreId;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public String getScreenId() {
        return screenId;
    }

    public String getScreenName() {
        return screenName;
    }
}
