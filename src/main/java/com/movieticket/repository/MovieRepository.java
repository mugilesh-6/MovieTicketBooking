package com.movieticket.repository;

import com.movieticket.model.Movie;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MovieRepository {
    private final List<Movie> movies = new ArrayList<>();

    public void save(Movie movie) { movies.add(movie); }

    public Optional<Movie> findById(String movieId) {
        for (Movie movie : movies) {
            if (movie.getMovieId().equalsIgnoreCase(movieId)) return Optional.of(movie);
        }
        return Optional.empty();
    }

    public List<Movie> findAll() { return movies; }

    public void update(Movie movie) {
        for (int i = 0; i < movies.size(); i++) {
            if (movies.get(i).getMovieId().equalsIgnoreCase(movie.getMovieId())) {
                movies.set(i, movie);
                return;
            }
        }
    }

    public boolean exists(String movieId) { return findById(movieId).isPresent(); }
}
