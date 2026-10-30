package com.movieticket.model;

import com.movieticket.enums.Certificate;
import com.movieticket.enums.Genre;
import java.time.LocalDate;

public class Movie {
    private String movieId;
    private String title;
    private String language;
    private Genre genre;
    private int durationMinutes;
    private LocalDate releaseDate;
    private Certificate certificate;
    private double averageRating;

    public Movie() {}

    public Movie(String movieId, String title, String language, Genre genre, int durationMinutes, LocalDate releaseDate, Certificate certificate) {
        this.movieId = movieId;
        this.title = title;
        this.language = language;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
        this.releaseDate = releaseDate;
        this.certificate = certificate;
    }

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public Certificate getCertificate() { return certificate; }
    public void setCertificate(Certificate certificate) { this.certificate = certificate; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
}
