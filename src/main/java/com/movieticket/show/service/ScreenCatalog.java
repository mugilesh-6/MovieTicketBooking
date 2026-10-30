package com.movieticket.show.service;

import java.util.List;
import java.util.Optional;

public interface ScreenCatalog {
    Optional<ScreenDetails> findScreen(String theatreId, String screenId);

    List<ScreenDetails> findAllScreens();
}
