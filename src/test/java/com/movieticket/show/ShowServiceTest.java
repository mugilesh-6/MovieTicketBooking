package com.movieticket.show;

import com.movieticket.show.model.SeatType;
import com.movieticket.show.model.Show;
import com.movieticket.show.model.ShowPrices;
import com.movieticket.show.model.ShowStatus;
import com.movieticket.show.repository.ShowRepository;
import com.movieticket.show.service.BookingGateway;
import com.movieticket.show.service.MovieCatalog;
import com.movieticket.show.service.MovieDetails;
import com.movieticket.show.service.RefundGateway;
import com.movieticket.show.service.ScreenCatalog;
import com.movieticket.show.service.ScreenDetails;
import com.movieticket.show.service.SeatLockGateway;
import com.movieticket.show.service.ShowAccessRole;
import com.movieticket.show.service.ShowIntegrationException;
import com.movieticket.show.service.ShowService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ShowServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2030, 5, 1, 10, 0);
    private static final ShowPrices PRICES =
            new ShowPrices(new BigDecimal("100"), new BigDecimal("150"), new BigDecimal("200"));

    public static void main(String[] args) throws Exception {
        ShowServiceTest test = new ShowServiceTest();
        test.createsShowsAndCalculatesEndTime();
        test.rejectsOverlapAndInsufficientCleaningGap();
        test.acceptsExactlyThirtyMinuteCleaningGap();
        test.rejectsPastAndPreReleaseShows();
        test.rejectsDuplicateIdsAndInvalidPrices();
        test.enforcesCreationAndCancellationRoles();
        test.updatesPricesAndKeepsShowScheduleImmutable();
        test.cancellationInvokesAllIntegrationsAndIsRetryable();
        test.exposesIntegrationQueries();
        System.out.println("All ShowService tests passed.");
    }

    private void createsShowsAndCalculatesEndTime() {
        Fixture fixture = new Fixture();
        Show show = fixture.service.createShow("S1", "M1", "T1", "SC1",
                NOW.plusHours(1), PRICES, ShowAccessRole.THEATRE_MANAGER);
        check(show.getEndTime().equals(NOW.plusHours(3)), "Duration should determine end time");
        check(show.getStatus() == ShowStatus.SCHEDULED, "New show should be scheduled");
    }

    private void rejectsOverlapAndInsufficientCleaningGap() {
        Fixture fixture = new Fixture();
        fixture.create("S1", NOW.plusHours(1));
        expect(IllegalArgumentException.class,
                () -> fixture.create("S2", NOW.plusHours(2)));
        expect(IllegalArgumentException.class,
                () -> fixture.create("S3", NOW.plusHours(2).plusMinutes(29)));
    }

    private void acceptsExactlyThirtyMinuteCleaningGap() {
        Fixture fixture = new Fixture();
        fixture.create("S1", NOW.plusHours(1));
        Show next = fixture.create("S2", NOW.plusHours(3).plusMinutes(30));
        check(next.getStartTime().equals(NOW.plusHours(3).plusMinutes(30)),
                "A 30-minute gap should be accepted");
    }

    private void rejectsPastAndPreReleaseShows() {
        Fixture fixture = new Fixture();
        expect(IllegalArgumentException.class,
                () -> fixture.create("PAST", NOW.minusMinutes(1)));
        fixture.movies.put(new MovieDetails("FUTURE", "Future movie",
                NOW.toLocalDate().plusDays(1), 120));
        expect(IllegalArgumentException.class, () -> fixture.service.createShow(
                "EARLY", "FUTURE", "T1", "SC1", NOW.plusHours(1), PRICES,
                ShowAccessRole.ADMIN));
    }

    private void rejectsDuplicateIdsAndInvalidPrices() {
        Fixture fixture = new Fixture();
        fixture.create("S1", NOW.plusHours(1));
        expect(IllegalArgumentException.class, () -> fixture.create("S1", NOW.plusHours(5)));
        expect(IllegalArgumentException.class, () -> fixture.service.createShow(
                "BAD_PRICE", "M1", "T1", "SC1", NOW.plusHours(5),
                new ShowPrices(BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.TEN),
                ShowAccessRole.ADMIN));
        expect(IllegalArgumentException.class, () -> fixture.service.createShow(
                "MISSING_SCREEN", "M1", "T1", "missing", NOW.plusHours(5), PRICES,
                ShowAccessRole.ADMIN));
    }

    private void enforcesCreationAndCancellationRoles() {
        Fixture fixture = new Fixture();
        expect(SecurityException.class, () -> fixture.service.createShow(
                "S1", "M1", "T1", "SC1", NOW.plusHours(1), PRICES,
                ShowAccessRole.CUSTOMER));
        fixture.create("S1", NOW.plusHours(1));
        expect(SecurityException.class,
                () -> fixture.service.cancelShow("S1", ShowAccessRole.THEATRE_MANAGER));
    }

    private void updatesPricesAndKeepsShowScheduleImmutable() {
        Fixture fixture = new Fixture();
        Show original = fixture.create("S1", NOW.plusHours(1));
        Show updated = fixture.service.updatePrices("S1",
                new ShowPrices(new BigDecimal("101"), new BigDecimal("151"),
                        new BigDecimal("201")));
        check(updated.getStartTime().equals(original.getStartTime()), "Price update changed time");
        check(updated.getMovieId().equals(original.getMovieId()), "Price update changed movie");
        check(updated.getPrices().getPrice(SeatType.REGULAR)
                .compareTo(new BigDecimal("101")) == 0, "Price was not updated");
        expect(IllegalArgumentException.class, () -> fixture.service.updatePrices("S1",
                new ShowPrices(BigDecimal.ONE.negate(), BigDecimal.ONE, BigDecimal.ONE)));
    }

    private void cancellationInvokesAllIntegrationsAndIsRetryable() throws Exception {
        Fixture fixture = new Fixture();
        fixture.create("S1", NOW.plusHours(1));
        fixture.refunds.failOnce = true;
        expect(ShowIntegrationException.class,
                () -> fixture.service.cancelShow("S1", ShowAccessRole.ADMIN));
        check(fixture.service.getShowById("S1").get().getStatus()
                == ShowStatus.CANCELLATION_PENDING, "Failed cancellation must remain pending");
        Show cancelled = fixture.service.cancelShow("S1", ShowAccessRole.ADMIN);
        check(cancelled.getStatus() == ShowStatus.CANCELLED, "Retry should complete cancellation");
        check(fixture.booking.cancelled.contains("S1"), "Bookings were not cancelled");
        check(fixture.refunds.refunded.contains("S1"), "Refunds were not requested");
        check(fixture.locks.invalidated.contains("S1"), "Seat locks were not invalidated");
    }

    private void exposesIntegrationQueries() {
        Fixture fixture = new Fixture();
        fixture.create("S1", NOW.plusHours(1));
        fixture.booking.booked.add("S1");
        fixture.booking.theatresWithBookings.add("T1");
        check(fixture.service.screenHasShows("T1", "SC1"), "Screen lookup failed");
        check(fixture.service.theatreHasUpcomingShowsWithBookings("T1"),
                "Upcoming booking lookup failed");
        check(fixture.service.showHasBookings("S1"), "Booking lookup failed");
        check(fixture.service.getMovieForShow("S1").isPresent(), "Movie lookup failed");
        check(fixture.service.getScreenForShow("S1").isPresent(), "Screen details lookup failed");
        check(fixture.service.getCurrentSeatPrice("S1", SeatType.PREMIUM)
                .compareTo(new BigDecimal("150")) == 0, "Current seat price lookup failed");
        check(fixture.service.getShowStartTime("S1").isPresent(), "Start time lookup failed");
        check(fixture.service.getShowEndTime("S1").isPresent(), "End time lookup failed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expect(Class<? extends Throwable> expected, ThrowingRunnable action) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (expected.isInstance(actual)) {
                return;
            }
            throw new AssertionError("Expected " + expected.getSimpleName()
                    + " but got " + actual, actual);
        }
        throw new AssertionError("Expected " + expected.getSimpleName() + " to be thrown");
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static final class Fixture {
        private final InMemoryMovies movies = new InMemoryMovies();
        private final InMemoryBookings booking = new InMemoryBookings();
        private final InMemoryRefunds refunds = new InMemoryRefunds();
        private final InMemoryLocks locks = new InMemoryLocks();
        private final ShowService service;

        private Fixture() {
            movies.put(new MovieDetails("M1", "Test Movie", LocalDate.of(2020, 1, 1), 120));
            service = new ShowService(new ShowRepository(), movies, new InMemoryScreens(),
                    booking, refunds, locks,
                    Clock.fixed(Instant.parse("2030-05-01T10:00:00Z"), ZoneOffset.UTC));
        }

        private Show create(String id, LocalDateTime start) {
            return service.createShow(id, "M1", "T1", "SC1", start, PRICES,
                    ShowAccessRole.ADMIN);
        }
    }

    private static final class InMemoryMovies implements MovieCatalog {
        private final java.util.Map<String, MovieDetails> movies =
                new java.util.HashMap<String, MovieDetails>();

        private void put(MovieDetails movie) {
            movies.put(movie.getId(), movie);
        }

        @Override
        public Optional<MovieDetails> findMovieById(String movieId) {
            return Optional.ofNullable(movies.get(movieId));
        }

        @Override
        public List<MovieDetails> findAllMovies() {
            return Collections.unmodifiableList(
                    new java.util.ArrayList<MovieDetails>(movies.values()));
        }
    }

    private static final class InMemoryScreens implements ScreenCatalog {
        private final ScreenDetails screen = new ScreenDetails("T1", "Test Theatre",
                "SC1", "Screen 1");

        @Override
        public Optional<ScreenDetails> findScreen(String theatreId, String screenId) {
            return screen.getTheatreId().equals(theatreId)
                    && screen.getScreenId().equals(screenId)
                    ? Optional.of(screen) : Optional.<ScreenDetails>empty();
        }

        @Override
        public List<ScreenDetails> findAllScreens() {
            return Arrays.asList(screen);
        }
    }

    private static final class InMemoryBookings implements BookingGateway {
        private final Set<String> booked = new HashSet<String>();
        private final Set<String> cancelled = new HashSet<String>();
        private final Set<String> theatresWithBookings = new HashSet<String>();

        @Override
        public boolean hasBookingsForShow(String showId) {
            return booked.contains(showId);
        }

        @Override
        public void cancelBookingsForShow(String showId) {
            cancelled.add(showId);
        }

        @Override
        public boolean theatreHasUpcomingBookings(String theatreId) {
            return theatresWithBookings.contains(theatreId);
        }
    }

    private static final class InMemoryRefunds implements RefundGateway {
        private final Set<String> refunded = new HashSet<String>();
        private boolean failOnce;

        @Override
        public void refundAllForShow(String showId) throws ShowIntegrationException {
            if (failOnce) {
                failOnce = false;
                throw new ShowIntegrationException("Temporary refund failure");
            }
            refunded.add(showId);
        }
    }

    private static final class InMemoryLocks implements SeatLockGateway {
        private final Set<String> invalidated = new HashSet<String>();

        @Override
        public void invalidateLocksForShow(String showId) {
            invalidated.add(showId);
        }
    }
}
