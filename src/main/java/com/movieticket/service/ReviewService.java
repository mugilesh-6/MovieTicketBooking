package com.movieticket.service;

import com.movieticket.model.Movie;
import com.movieticket.model.Review;
import com.movieticket.repository.ReviewRepository;
import java.util.List;

public class ReviewService {
    private final ReviewRepository repository;
    private final MovieService movieService;

    public ReviewService(ReviewRepository repository, MovieService movieService) {
        this.repository = repository;
        this.movieService = movieService;
    }

    public Review addReview(Review review, boolean eligibleConfirmedEndedBooking) {
        if (review == null) throw new IllegalArgumentException("Review is required.");
        if (review.getReviewId() == null || review.getReviewId().trim().isEmpty()) throw new IllegalArgumentException("Review ID is required.");
        if (review.getUserId() == null || review.getUserId().trim().isEmpty()) throw new IllegalArgumentException("User ID is required.");
        if (review.getRating() < 1 || review.getRating() > 5) throw new IllegalArgumentException("Rating must be between 1 and 5.");
        if (!eligibleConfirmedEndedBooking) throw new IllegalArgumentException("User must have a confirmed booking for an ended show.");
        Movie movie = movieService.getMovieById(review.getMovieId());
        if (repository.existsByUserAndMovie(review.getUserId(), review.getMovieId())) throw new IllegalArgumentException("User has already reviewed this movie.");
        repository.save(review);
        updateAverageRating(movie);
        return review;
    }

    public List<Review> getReviewsForMovie(String movieId) { return repository.findByMovieId(movieId); }

    private void updateAverageRating(Movie movie) {
        List<Review> reviews = repository.findByMovieId(movie.getMovieId());
        double sum = 0;
        for (Review review : reviews) sum += review.getRating();
        movie.setAverageRating(reviews.isEmpty() ? 0 : sum / reviews.size());
    }
}
