package com.movieticket.show.cli;

import com.movieticket.show.model.SeatType;
import com.movieticket.show.model.Show;
import com.movieticket.show.model.ShowPrices;
import com.movieticket.show.service.MovieDetails;
import com.movieticket.show.service.ScreenDetails;
import com.movieticket.show.service.ShowAccessRole;
import com.movieticket.show.service.ShowIntegrationException;
import com.movieticket.show.service.ShowService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public final class ShowCli {
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Scanner scanner;
    private final Appendable output;
    private final ShowService showService;
    private final ShowAccessRole role;

    public ShowCli(Scanner scanner, Appendable output, ShowService showService,
                   ShowAccessRole role) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.output = Objects.requireNonNull(output, "output");
        this.showService = Objects.requireNonNull(showService, "showService");
        this.role = Objects.requireNonNull(role, "role");
    }

    public void run() {
        boolean running = true;
        while (running) {
            try {
                print("\n--- Show Scheduling ---\n"
                        + "1. Create show\n"
                        + "2. View all shows\n"
                        + "3. Search show by ID\n"
                        + "4. View shows by movie\n"
                        + "5. View shows by theatre\n"
                        + "6. Update show prices\n"
                        + "7. Cancel show\n"
                        + "0. Exit\n"
                        + "Choice: ");
                String choice = readLine();
                switch (choice) {
                    case "1":
                        createShow();
                        break;
                    case "2":
                        printShows(showService.getAllShows());
                        break;
                    case "3":
                        printShow(showService.getShowById(prompt("Show ID: "))
                                .orElse(null));
                        break;
                    case "4":
                        printShows(showService.getShowsByMovie(prompt("Movie ID: ")));
                        break;
                    case "5":
                        printShows(showService.getShowsByTheatre(prompt("Theatre ID: ")));
                        break;
                    case "6":
                        updatePrices();
                        break;
                    case "7":
                        cancelShow();
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        print("Choose a listed menu option.\n");
                }
            } catch (IllegalArgumentException | IllegalStateException | SecurityException
                     | ShowIntegrationException exception) {
                try {
                    print("Operation failed: " + exception.getMessage() + "\n");
                } catch (java.io.IOException outputException) {
                    throw new IllegalStateException("Unable to write CLI output", outputException);
                }
            } catch (java.io.IOException exception) {
                throw new IllegalStateException("Unable to write CLI output", exception);
            }
        }
    }

    private void createShow() throws java.io.IOException {
        print("Available movies:\n");
        for (MovieDetails movie : showService.getAvailableMovies()) {
            print("  " + movie.getId() + " - " + movie.getTitle() + "\n");
        }
        print("Available screens:\n");
        for (ScreenDetails screen : showService.getAvailableScreens()) {
            print("  " + screen.getTheatreId() + " / " + screen.getScreenId()
                    + " - " + screen.getTheatreName() + " / " + screen.getScreenName() + "\n");
        }

        String showId = prompt("Show ID: ");
        String movieId = prompt("Movie ID: ");
        String theatreId = prompt("Theatre ID: ");
        String screenId = prompt("Screen ID: ");
        LocalDateTime startTime;
        try {
            startTime = LocalDateTime.parse(prompt("Start (yyyy-MM-dd HH:mm): "),
                    DATE_TIME_FORMAT);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Start time must use yyyy-MM-dd HH:mm");
        }
        ShowPrices prices = readPrices();
        Show show = showService.createShow(showId, movieId, theatreId, screenId,
                startTime, prices, role);
        print("Show created:\n");
        printShow(show);
    }

    private void updatePrices() throws java.io.IOException {
        String showId = prompt("Show ID: ");
        Show updated = showService.updatePrices(showId, readPrices());
        print("Prices updated for show " + updated.getId() + ".\n");
    }

    private void cancelShow() throws ShowIntegrationException, java.io.IOException {
        Show cancelled = showService.cancelShow(prompt("Show ID: "), role);
        print("Show " + cancelled.getId() + " status: " + cancelled.getStatus() + "\n");
    }

    private ShowPrices readPrices() throws java.io.IOException {
        return new ShowPrices(
                new BigDecimal(prompt("Regular price: ")),
                new BigDecimal(prompt("Premium price: ")),
                new BigDecimal(prompt("Recliner price: ")));
    }

    private void printShows(List<Show> shows) throws java.io.IOException {
        if (shows.isEmpty()) {
            print("No shows found.\n");
            return;
        }
        for (Show show : shows) {
            printShow(show);
        }
    }

    private void printShow(Show show) throws java.io.IOException {
        if (show == null) {
            print("Show not found.\n");
            return;
        }
        print(show.getId() + " | movie=" + show.getMovieId()
                + " | theatre=" + show.getTheatreId()
                + " | screen=" + show.getScreenId()
                + " | " + show.getStartTime() + " - " + show.getEndTime()
                + " | status=" + show.getStatus()
                + " | Regular=" + show.getPrices().getPrice(SeatType.REGULAR)
                + " Premium=" + show.getPrices().getPrice(SeatType.PREMIUM)
                + " Recliner=" + show.getPrices().getPrice(SeatType.RECLINER) + "\n");
    }

    private String prompt(String message) throws java.io.IOException {
        print(message);
        return readLine();
    }

    private String readLine() {
        if (!scanner.hasNextLine()) {
            return "0";
        }
        return scanner.nextLine().trim();
    }

    private void print(String value) throws java.io.IOException {
        output.append(value);
    }
}
