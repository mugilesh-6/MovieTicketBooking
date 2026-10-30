package com.movieticket.service;

import com.movieticket.enums.SeatType;
import com.movieticket.enums.UserRole;
import com.movieticket.model.Screen;
import com.movieticket.model.Seat;
import com.movieticket.model.Theatre;
import com.movieticket.repository.TheatreRepository;
import java.util.List;

public class TheatreService {
    private final TheatreRepository repository;

    public TheatreService(TheatreRepository repository) { this.repository = repository; }

    public Theatre addTheatre(Theatre theatre, UserRole role) {
        requireAdmin(role);
        if (theatre == null) throw new IllegalArgumentException("Theatre is required.");
        requireText(theatre.getTheatreId(), "Theatre ID");
        requireText(theatre.getTheatreName(), "Theatre name");
        requireText(theatre.getCity(), "City");
        if (repository.findById(theatre.getTheatreId()).isPresent()) throw new IllegalArgumentException("Theatre ID already exists.");
        repository.save(theatre);
        return theatre;
    }

    public List<Theatre> getAllTheatres() { return repository.findAll(); }

    public Theatre getTheatreById(String theatreId) {
        requireText(theatreId, "Theatre ID");
        return repository.findById(theatreId).orElseThrow(() -> new IllegalArgumentException("Theatre not found: " + theatreId));
    }

    public Screen addScreen(String theatreId, Screen screen, UserRole role) {
        requireAdmin(role);
        Theatre theatre = getTheatreById(theatreId);
        if (screen == null) throw new IllegalArgumentException("Screen is required.");
        requireText(screen.getScreenId(), "Screen ID");
        requireText(screen.getScreenName(), "Screen name");
        if (screen.getNumberOfRows() <= 0 || screen.getSeatsPerRow() <= 0) throw new IllegalArgumentException("Rows and seats per row must be greater than zero.");
        for (Screen existing : theatre.getScreens()) {
            if (existing.getScreenId().equalsIgnoreCase(screen.getScreenId())) throw new IllegalArgumentException("Screen ID already exists in this theatre.");
        }
        screen.generateSeats();
        theatre.getScreens().add(screen);
        return screen;
    }

    public void changeSeatType(String theatreId, String screenId, String seatId, SeatType seatType, boolean hasShows, UserRole role) {
        requireAdmin(role);
        if (hasShows) throw new IllegalArgumentException("Seat type cannot be changed after a show has been created.");
        Screen screen = findScreen(theatreId, screenId);
        for (Seat seat : screen.getSeats()) {
            if (seat.getSeatId().equalsIgnoreCase(seatId)) {
                if (seatType == null) throw new IllegalArgumentException("Seat type is required.");
                seat.setSeatType(seatType);
                return;
            }
        }
        throw new IllegalArgumentException("Seat not found: " + seatId);
    }

    public void deleteTheatre(String theatreId, boolean hasUpcomingShowsWithBookings, UserRole role) {
        requireAdmin(role);
        getTheatreById(theatreId);
        if (hasUpcomingShowsWithBookings) throw new IllegalArgumentException("Theatre cannot be deleted because it has upcoming shows with bookings.");
        repository.delete(theatreId);
    }

    private Screen findScreen(String theatreId, String screenId) {
        requireText(screenId, "Screen ID");
        Theatre theatre = getTheatreById(theatreId);
        for (Screen screen : theatre.getScreens()) {
            if (screen.getScreenId().equalsIgnoreCase(screenId)) return screen;
        }
        throw new IllegalArgumentException("Screen not found: " + screenId);
    }

    private void requireAdmin(UserRole role) {
        if (role != UserRole.ADMIN) throw new SecurityException("Only an administrator can perform this operation.");
    }

    private void requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(name + " is required.");
    }
}
