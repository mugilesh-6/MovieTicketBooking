package com.movieticket.service;

import com.movieticket.enums.UserRole;
import com.movieticket.model.Movie;
import com.movieticket.repository.MovieRepository;
import java.util.List;

public class MovieService {
    private final MovieRepository repository;

    public MovieService(MovieRepository repository) { this.repository = repository; }

    public Movie addMovie(Movie movie, UserRole role) {
        requireAdmin(role);
        validate(movie);
        if (repository.exists(movie.getMovieId())) throw new IllegalArgumentException("Movie ID already exists.");
        repository.save(movie);
        return movie;
    }

    public List<Movie> getAllMovies() { return repository.findAll(); }

    public Movie getMovieById(String movieId) {
        requireText(movieId, "Movie ID");
        return repository.findById(movieId).orElseThrow(() -> new IllegalArgumentException("Movie not found: " + movieId));
    }

    public Movie updateMovie(String movieId, Movie updatedMovie, UserRole role) {
        requireAdmin(role);
        Movie current = getMovieById(movieId);
        validate(updatedMovie);
        current.setTitle(updatedMovie.getTitle());
        current.setLanguage(updatedMovie.getLanguage());
        current.setGenre(updatedMovie.getGenre());
        current.setDurationMinutes(updatedMovie.getDurationMinutes());
        current.setReleaseDate(updatedMovie.getReleaseDate());
        current.setCertificate(updatedMovie.getCertificate());
        repository.update(current);
        return current;
    }

    private void validate(Movie movie) {
        if (movie == null) throw new IllegalArgumentException("Movie is required.");
        requireText(movie.getMovieId(), "Movie ID");
        requireText(movie.getTitle(), "Title");
        requireText(movie.getLanguage(), "Language");
        if (movie.getGenre() == null) throw new IllegalArgumentException("Genre is required.");
        if (movie.getDurationMinutes() <= 0) throw new IllegalArgumentException("Duration must be greater than zero.");
        if (movie.getReleaseDate() == null) throw new IllegalArgumentException("Release date is required.");
        if (movie.getCertificate() == null) throw new IllegalArgumentException("Certificate is required.");
    }

    private void requireAdmin(UserRole role) {
        if (role != UserRole.ADMIN) throw new SecurityException("Only an administrator can perform this operation.");
    }

    private void requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(name + " is required.");
    }
}
