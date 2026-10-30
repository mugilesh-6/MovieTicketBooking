package com.movieticket;

import com.movieticket.cli.AdminCLI;
import com.movieticket.repository.MovieRepository;
import com.movieticket.repository.TheatreRepository;
import com.movieticket.repository.ReviewRepository;
import com.movieticket.service.MovieService;
import com.movieticket.service.TheatreService;
import com.movieticket.service.ReviewService;

public class Main {

    public static void main(String[] args) {

        // Repository Objects
        MovieRepository movieRepository =
                new MovieRepository();

        TheatreRepository theatreRepository =
                new TheatreRepository();

        ReviewRepository reviewRepository =
                new ReviewRepository();

        // Service Objects
        MovieService movieService =
                new MovieService(movieRepository);

        TheatreService theatreService =
                new TheatreService(theatreRepository);

        ReviewService reviewService =
                new ReviewService(
                        reviewRepository,
                        movieRepository
                );

        // CLI Object
        AdminCLI adminCLI =
                new AdminCLI(
                        movieService,
                        theatreService,
                        reviewService
                );

        System.out.println("==================================");
        System.out.println("   MOVIE TICKET BOOKING SYSTEM");
        System.out.println("==================================");

        adminCLI.start();
    }
}