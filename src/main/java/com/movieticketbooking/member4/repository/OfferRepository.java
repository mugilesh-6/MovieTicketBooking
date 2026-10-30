package com.movieticketbooking.member4.repository;

import com.movieticketbooking.member4.model.Offer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OfferRepository {
    private final List<Offer> offers = new ArrayList<>();
    private final List<String> usedOfferKeys = new ArrayList<>();

    public synchronized void save(Offer offer) {
        if (findByCode(offer.code()).isPresent()) {
            throw new IllegalArgumentException("Offer code already exists: " + offer.code());
        }
        offers.add(offer);
    }

    public synchronized Optional<Offer> findByCode(String code) {
        if (code == null) return Optional.empty();
        return offers.stream().filter(offer -> offer.code().equalsIgnoreCase(code.trim())).findFirst();
    }

    public synchronized boolean hasUserUsedOffer(String userId, String offerCode) {
        return usedOfferKeys.contains(usageKey(userId, offerCode));
    }

    public synchronized void markOfferUsed(String userId, String offerCode) {
        String key = usageKey(userId, offerCode);
        if (usedOfferKeys.contains(key)) {
            throw new IllegalStateException("User has already used this offer");
        }
        usedOfferKeys.add(key);
    }

    private String usageKey(String userId, String offerCode) {
        return userId + "\u0000" + offerCode.toUpperCase(java.util.Locale.ROOT);
    }
}