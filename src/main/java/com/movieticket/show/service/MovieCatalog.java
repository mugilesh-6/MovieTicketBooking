package com.movieticket.show.service;

import java.util.List;
import java.util.Optional;

public interface MovieCatalog {
    Optional<MovieDetails> findMovieById(String movieId);

    List<MovieDetails> findAllMovies();
}
