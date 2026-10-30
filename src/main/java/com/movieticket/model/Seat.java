package com.movieticket.model;

import com.movieticket.enums.SeatType;

public class Seat {
    private String seatId;
    private SeatType seatType;

    public Seat() {}

    public Seat(String seatId) {
        this.seatId = seatId;
        this.seatType = SeatType.REGULAR;
    }

    public Seat(String seatId, SeatType seatType) {
        this.seatId = seatId;
        this.seatType = seatType;
    }

    public String getSeatId() { return seatId; }
    public void setSeatId(String seatId) { this.seatId = seatId; }
    public SeatType getSeatType() { return seatType; }
    public void setSeatType(SeatType seatType) { this.seatType = seatType; }
}
