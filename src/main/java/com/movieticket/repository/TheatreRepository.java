package com.movieticket.repository;

import com.movieticket.model.Theatre;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TheatreRepository {
    private final List<Theatre> theatres = new ArrayList<>();

    public void save(Theatre theatre) { theatres.add(theatre); }

    public Optional<Theatre> findById(String theatreId) {
        for (Theatre theatre : theatres) {
            if (theatre.getTheatreId().equalsIgnoreCase(theatreId)) return Optional.of(theatre);
        }
        return Optional.empty();
    }

    public List<Theatre> findAll() { return theatres; }

    public void delete(String theatreId) {
        theatres.removeIf(theatre -> theatre.getTheatreId().equalsIgnoreCase(theatreId));
    }
}
