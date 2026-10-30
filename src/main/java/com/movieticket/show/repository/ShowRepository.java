package com.movieticket.show.repository;

import com.movieticket.show.model.Show;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ShowRepository {
    private final List<Show> shows = new ArrayList<Show>();

    public synchronized boolean saveIfAbsent(Show show) {
        if (findById(show.getId()).isPresent()) {
            return false;
        }
        shows.add(show);
        return true;
    }

    public synchronized Optional<Show> findById(String showId) {
        for (Show show : shows) {
            if (show.getId().equals(showId)) {
                return Optional.of(show);
            }
        }
        return Optional.empty();
    }

    public synchronized List<Show> findAll() {
        return Collections.unmodifiableList(new ArrayList<Show>(shows));
    }

    public synchronized List<Show> findByMovieId(String movieId) {
        List<Show> result = new ArrayList<Show>();
        for (Show show : shows) {
            if (show.getMovieId().equals(movieId)) {
                result.add(show);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<Show> findByTheatreId(String theatreId) {
        List<Show> result = new ArrayList<Show>();
        for (Show show : shows) {
            if (show.getTheatreId().equals(theatreId)) {
                result.add(show);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized boolean hasAnyShowsForScreen(String theatreId, String screenId) {
        for (Show show : shows) {
            if (show.getTheatreId().equals(theatreId)
                    && show.getScreenId().equals(screenId)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean replace(Show updatedShow) {
        for (int i = 0; i < shows.size(); i++) {
            if (shows.get(i).getId().equals(updatedShow.getId())) {
                shows.set(i, updatedShow);
                return true;
            }
        }
        return false;
    }
}
