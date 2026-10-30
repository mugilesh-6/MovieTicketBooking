package com.movieticket.model;

import java.util.ArrayList;
import java.util.List;

public class Theatre {
    private String theatreId;
    private String theatreName;
    private String city;
    private List<Screen> screens;

    public Theatre() { screens = new ArrayList<>(); }

    public Theatre(String theatreId, String theatreName, String city) {
        this.theatreId = theatreId;
        this.theatreName = theatreName;
        this.city = city;
        screens = new ArrayList<>();
    }

    public String getTheatreId() { return theatreId; }
    public void setTheatreId(String theatreId) { this.theatreId = theatreId; }
    public String getTheatreName() { return theatreName; }
    public void setTheatreName(String theatreName) { this.theatreName = theatreName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public List<Screen> getScreens() { return screens; }
    public void setScreens(List<Screen> screens) { this.screens = screens == null ? new ArrayList<>() : screens; }
}
