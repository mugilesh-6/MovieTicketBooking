package com.movieticket.show.service;

import com.movieticket.show.model.SeatType;
import com.movieticket.show.model.Show;
import com.movieticket.show.model.ShowPrices;
import com.movieticket.show.model.ShowStatus;
import com.movieticket.show.repository.ShowRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ShowService {
    private static final long CLEANING_GAP_MINUTES = 30L;

    private final ShowRepository showRepository;
    private final MovieCatalog movieCatalog;
    private final ScreenCatalog screenCatalog;
    private final BookingGateway bookingGateway;
    private final RefundGateway refundGateway;
    private final SeatLockGateway seatLockGateway;
    private final Clock clock;

    public ShowService(ShowRepository showRepository, MovieCatalog movieCatalog,
                       ScreenCatalog screenCatalog, BookingGateway bookingGateway,
                       RefundGateway refundGateway, SeatLockGateway seatLockGateway) {
        this(showRepository, movieCatalog, screenCatalog, bookingGateway,
                refundGateway, seatLockGateway, Clock.systemDefaultZone());
    }

    public ShowService(ShowRepository showRepository, MovieCatalog movieCatalog,
                       ScreenCatalog screenCatalog, BookingGateway bookingGateway,
                       RefundGateway refundGateway, SeatLockGateway seatLockGateway,
                       Clock clock) {
        this.showRepository = Objects.requireNonNull(showRepository, "showRepository");
        this.movieCatalog = Objects.requireNonNull(movieCatalog, "movieCatalog");
        this.screenCatalog = Objects.requireNonNull(screenCatalog, "screenCatalog");
        this.bookingGateway = Objects.requireNonNull(bookingGateway, "bookingGateway");
        this.refundGateway = Objects.requireNonNull(refundGateway, "refundGateway");
        this.seatLockGateway = Objects.requireNonNull(seatLockGateway, "seatLockGateway");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public synchronized Show createShow(String showId, String movieId, String theatreId,
                                        String screenId, LocalDateTime startTime,
                                        ShowPrices prices, ShowAccessRole role) {
        requireRole(role, ShowAccessRole.ADMIN, ShowAccessRole.THEATRE_MANAGER);
        validatePrices(prices);
        if (showRepository.findById(showId).isPresent()) {
            throw new IllegalArgumentException("Show ID already exists: " + showId);
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Show start time is required");
        }
        if (startTime.isBefore(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException("Show date and time cannot be in the past");
        }

        MovieDetails movie = movieCatalog.findMovieById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found: " + movieId));
        if (startTime.toLocalDate().isBefore(movie.getReleaseDate())) {
            throw new IllegalArgumentException("Show date cannot be before movie release date");
        }

        ScreenDetails screen = screenCatalog.findScreen(theatreId, screenId)
                .orElseThrow(() -> new IllegalArgumentException("Screen not found"));
        LocalDateTime endTime = startTime.plusMinutes(movie.getDurationMinutes());
        ensureNoScreenConflict(theatreId, screenId, startTime, endTime);

        Show show = new Show(showId, movieId, screen.getTheatreId(), screen.getScreenId(),
                startTime, endTime, prices, ShowStatus.SCHEDULED);
        if (!showRepository.saveIfAbsent(show)) {
            throw new IllegalArgumentException("Show ID already exists: " + showId);
        }
        return show;
    }

    public List<Show> getAllShows() {
        return showRepository.findAll();
    }

    public Optional<Show> getShowById(String showId) {
        return showRepository.findById(showId);
    }

    public List<Show> getShowsByMovie(String movieId) {
        return showRepository.findByMovieId(movieId);
    }

    public List<Show> getShowsByTheatre(String theatreId) {
        return showRepository.findByTheatreId(theatreId);
    }

    public synchronized Show updatePrices(String showId, ShowPrices prices) {
        validatePrices(prices);
        Show show = requireShow(showId);
        if (show.getStatus() != ShowStatus.SCHEDULED) {
            throw new IllegalStateException("Prices can only be updated for scheduled shows");
        }
        Show updated = show.withPrices(prices);
        showRepository.replace(updated);
        return updated;
    }

    public synchronized Show cancelShow(String showId, ShowAccessRole role)
            throws ShowIntegrationException {
        requireRole(role, ShowAccessRole.ADMIN);
        Show show = requireShow(showId);
        if (show.getStatus() == ShowStatus.CANCELLED) {
            return show;
        }
        if (show.getStatus() != ShowStatus.CANCELLATION_PENDING) {
            show = show.withStatus(ShowStatus.CANCELLATION_PENDING);
            showRepository.replace(show);
        }

        bookingGateway.cancelBookingsForShow(showId);
        refundGateway.refundAllForShow(showId);
        seatLockGateway.invalidateLocksForShow(showId);

        Show cancelled = show.withStatus(ShowStatus.CANCELLED);
        showRepository.replace(cancelled);
        return cancelled;
    }

    public boolean screenHasShows(String theatreId, String screenId) {
        return showRepository.hasAnyShowsForScreen(theatreId, screenId);
    }

    public boolean theatreHasUpcomingShowsWithBookings(String theatreId) {
        LocalDateTime now = LocalDateTime.now(clock);
        boolean hasUpcomingShow = false;
        for (Show show : showRepository.findByTheatreId(theatreId)) {
            if (show.getStatus() == ShowStatus.SCHEDULED && show.getStartTime().isAfter(now)) {
                hasUpcomingShow = true;
                break;
            }
        }
        return hasUpcomingShow && bookingGateway.theatreHasUpcomingBookings(theatreId);
    }

    public Optional<LocalDateTime> getShowStartTime(String showId) {
        return getShowById(showId).map(Show::getStartTime);
    }

    public Optional<LocalDateTime> getShowEndTime(String showId) {
        return getShowById(showId).map(Show::getEndTime);
    }

    public Optional<MovieDetails> getMovieForShow(String showId) {
        return getShowById(showId).flatMap(show -> movieCatalog.findMovieById(show.getMovieId()));
    }

    public Optional<ScreenDetails> getScreenForShow(String showId) {
        return getShowById(showId).flatMap(show ->
                screenCatalog.findScreen(show.getTheatreId(), show.getScreenId()));
    }

    public boolean showHasBookings(String showId) {
        requireShow(showId);
        return bookingGateway.hasBookingsForShow(showId);
    }

    public BigDecimal getCurrentSeatPrice(String showId, SeatType seatType) {
        return requireShow(showId).getPrices().getPrice(seatType);
    }

    public List<MovieDetails> getAvailableMovies() {
        return movieCatalog.findAllMovies();
    }

    public List<ScreenDetails> getAvailableScreens() {
        return screenCatalog.findAllScreens();
    }

    private Show requireShow(String showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new IllegalArgumentException("Show not found: " + showId));
    }

    private void ensureNoScreenConflict(String theatreId, String screenId,
                                        LocalDateTime startTime, LocalDateTime endTime) {
        for (Show existing : showRepository.findByTheatreId(theatreId)) {
            if (!existing.getScreenId().equals(screenId)
                    || existing.getStatus() == ShowStatus.CANCELLED) {
                continue;
            }
            boolean overlapsWithCleaningGap =
                    startTime.isBefore(existing.getEndTime().plusMinutes(CLEANING_GAP_MINUTES))
                            && existing.getStartTime().isBefore(
                                    endTime.plusMinutes(CLEANING_GAP_MINUTES));
            if (overlapsWithCleaningGap) {
                throw new IllegalArgumentException(
                        "Show conflicts with show " + existing.getId()
                                + "; a 30-minute cleaning gap is required");
            }
        }
    }

    private void validatePrices(ShowPrices prices) {
        if (prices == null) {
            throw new IllegalArgumentException("Prices are required");
        }
        for (SeatType seatType : SeatType.values()) {
            BigDecimal price = prices.getPrice(seatType);
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        seatType + " price must be greater than zero");
            }
        }
    }

    private void requireRole(ShowAccessRole actual, ShowAccessRole... allowed) {
        if (actual == null) {
            throw new SecurityException("An authenticated role is required");
        }
        for (ShowAccessRole role : allowed) {
            if (actual == role) {
                return;
            }
        }
        throw new SecurityException("Role " + actual + " is not authorized for this operation");
    }
}
