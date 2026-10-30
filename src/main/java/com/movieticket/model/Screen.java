package com.movieticket.model;

import java.util.ArrayList;
import java.util.List;

public class Screen {
    private String screenId;
    private String screenName;
    private int numberOfRows;
    private int seatsPerRow;
    private List<Seat> seats;

    public Screen() { seats = new ArrayList<>(); }

    public Screen(String screenId, String screenName, int numberOfRows, int seatsPerRow) {
        this.screenId = screenId;
        this.screenName = screenName;
        this.numberOfRows = numberOfRows;
        this.seatsPerRow = seatsPerRow;
        seats = new ArrayList<>();
        generateSeats();
    }

    public final void generateSeats() {
        seats.clear();
        for (int row = 1; row <= numberOfRows; row++) {
            char rowLabel = (char) ('A' + row - 1);
            for (int seatNumber = 1; seatNumber <= seatsPerRow; seatNumber++) {
                seats.add(new Seat(screenId + "-" + rowLabel + seatNumber));
            }
        }
    }

    public String getScreenId() { return screenId; }
    public void setScreenId(String screenId) { this.screenId = screenId; }
    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }
    public int getNumberOfRows() { return numberOfRows; }
    public void setNumberOfRows(int numberOfRows) { this.numberOfRows = numberOfRows; }
    public int getSeatsPerRow() { return seatsPerRow; }
    public void setSeatsPerRow(int seatsPerRow) { this.seatsPerRow = seatsPerRow; }
    public List<Seat> getSeats() { return seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats == null ? new ArrayList<>() : seats; }
}
