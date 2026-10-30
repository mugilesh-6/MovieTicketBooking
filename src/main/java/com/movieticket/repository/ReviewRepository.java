package com.movieticket.repository;

import com.movieticket.model.Review;
import java.util.ArrayList;
import java.util.List;

public class ReviewRepository {
    private final List<Review> reviews = new ArrayList<>();

    public void save(Review review) { reviews.add(review); }

    public List<Review> findByMovieId(String movieId) {
        List<Review> result = new ArrayList<>();
        for (Review review : reviews) {
            if (review.getMovieId().equalsIgnoreCase(movieId)) result.add(review);
        }
        return result;
    }

    public boolean existsByUserAndMovie(String userId, String movieId) {
        for (Review review : reviews) {
            if (review.getUserId().equalsIgnoreCase(userId) && review.getMovieId().equalsIgnoreCase(movieId)) return true;
        }
        return false;
    }
}
