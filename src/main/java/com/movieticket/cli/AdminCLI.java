package com.movieticket.cli;

import com.movieticket.enums.Certificate;
import com.movieticket.enums.Genre;
import com.movieticket.enums.SeatType;
import com.movieticket.enums.UserRole;
import com.movieticket.model.Movie;
import com.movieticket.model.Review;
import com.movieticket.model.Screen;
import com.movieticket.model.Theatre;
import com.movieticket.repository.MovieRepository;
import com.movieticket.repository.ReviewRepository;
import com.movieticket.repository.TheatreRepository;
import com.movieticket.service.MovieService;
import com.movieticket.service.ReviewService;
import com.movieticket.service.TheatreService;
import java.time.LocalDate;
import java.util.Scanner;

public class AdminCLI {
    private final Scanner scanner = new Scanner(System.in);
    private final MovieRepository movieRepository = new MovieRepository();
    private final TheatreRepository theatreRepository = new TheatreRepository();
    private final ReviewRepository reviewRepository = new ReviewRepository();
    private final MovieService movieService = new MovieService(movieRepository);
    private final TheatreService theatreService = new TheatreService(theatreRepository);
    private final ReviewService reviewService = new ReviewService(reviewRepository, movieService);

    public static void main(String[] args) {
        new AdminCLI().start();
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\nMovie Ticket Booking System - Member 1");
            System.out.println("1. Movie menu");
            System.out.println("2. Theatre menu");
            System.out.println("3. Submit review");
            System.out.println("4. Exit");
            switch (read("Choice")) {
                case "1": movieMenu(); break;
                case "2": theatreMenu(); break;
                case "3": reviewMenu(); break;
                case "4": running = false; break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void movieMenu() {
        System.out.println("\n1. Add  2. View all  3. Search  4. Update  5. Back");
        try {
            switch (read("Choice")) {
                case "1":
                    Movie movie = new Movie(read("Movie ID"), read("Title"), read("Language"),
                        Genre.valueOf(read("Genre ACTION/COMEDY/DRAMA/THRILLER/ANIMATION").toUpperCase()),
                        Integer.parseInt(read("Duration minutes")), LocalDate.parse(read("Release date (YYYY-MM-DD)")),
                        Certificate.valueOf(read("Certificate U/UA/A").toUpperCase()));
                    movieService.addMovie(movie, UserRole.ADMIN);
                    System.out.println("Movie added.");
                    break;
                case "2":
                    for (Movie item : movieService.getAllMovies()) System.out.println(item.getMovieId() + " | " + item.getTitle() + " | " + item.getAverageRating());
                    break;
                case "3": System.out.println(movieService.getMovieById(read("Movie ID")));
                    break;
                case "4": updateMovie();
                    break;
                default:
            }
        } catch (Exception exception) {
            System.out.println("Error: " + exception.getMessage());
        }
    }

    private void updateMovie() {
        Movie current = movieService.getMovieById(read("Movie ID"));
        current.setTitle(read("New title"));
        current.setLanguage(read("New language"));
        current.setGenre(Genre.valueOf(read("New genre").toUpperCase()));
        current.setDurationMinutes(Integer.parseInt(read("New duration minutes")));
        current.setReleaseDate(LocalDate.parse(read("New release date (YYYY-MM-DD)")));
        current.setCertificate(Certificate.valueOf(read("New certificate U/UA/A").toUpperCase()));
        movieService.updateMovie(current.getMovieId(), current, UserRole.ADMIN);
        System.out.println("Movie updated.");
    }

    private void theatreMenu() {
        System.out.println("\n1. Add  2. View all  3. Add screen  4. Change seat type  5. Delete  6. Back");
        try {
            switch (read("Choice")) {
                case "1":
                    Theatre theatre = new Theatre(read("Theatre ID"), read("Theatre name"), read("City"));
                    theatreService.addTheatre(theatre, UserRole.ADMIN);
                    System.out.println("Theatre added.");
                    break;
                case "2":
                    for (Theatre item : theatreService.getAllTheatres()) System.out.println(item.getTheatreId() + " | " + item.getTheatreName() + " | " + item.getCity());
                    break;
                case "3":
                    Screen screen = new Screen(read("Screen ID"), read("Screen name"), Integer.parseInt(read("Number of rows")), Integer.parseInt(read("Seats per row")));
                    theatreService.addScreen(read("Theatre ID"), screen, UserRole.ADMIN);
                    System.out.println("Screen and " + screen.getSeats().size() + " seats added.");
                    break;
                case "4":
                    theatreService.changeSeatType(read("Theatre ID"), read("Screen ID"), read("Seat ID"),
                        SeatType.valueOf(read("Seat type REGULAR/PREMIUM/RECLINER").toUpperCase()),
                        Boolean.parseBoolean(read("Has a show been created? true/false")), UserRole.ADMIN);
                    System.out.println("Seat type updated.");
                    break;
                case "5":
                    theatreService.deleteTheatre(read("Theatre ID"), Boolean.parseBoolean(read("Has upcoming shows with bookings? true/false")), UserRole.ADMIN);
                    System.out.println("Theatre deleted.");
                    break;
                default:
            }
        } catch (Exception exception) {
            System.out.println("Error: " + exception.getMessage());
        }
    }

    private void reviewMenu() {
        try {
            Review review = new Review(read("Review ID"), read("User ID"), read("Movie ID"),
                Integer.parseInt(read("Rating 1-5")), read("Comment"));
            boolean eligible = Boolean.parseBoolean(read("Confirmed booking for an ended show? true/false"));
            reviewService.addReview(review, eligible);
            System.out.println("Review submitted.");
        } catch (Exception exception) {
            System.out.println("Error: " + exception.getMessage());
        }
    }

    private String read(String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
    }
}
