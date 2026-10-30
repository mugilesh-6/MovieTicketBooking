package com.movieticket.show.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class ShowPrices {
    private final Map<SeatType, BigDecimal> prices;

    public ShowPrices(BigDecimal regular, BigDecimal premium, BigDecimal recliner) {
        this.prices = new EnumMap<SeatType, BigDecimal>(SeatType.class);
        this.prices.put(SeatType.REGULAR, Objects.requireNonNull(regular, "regular price"));
        this.prices.put(SeatType.PREMIUM, Objects.requireNonNull(premium, "premium price"));
        this.prices.put(SeatType.RECLINER, Objects.requireNonNull(recliner, "recliner price"));
    }

    public BigDecimal getPrice(SeatType seatType) {
        return prices.get(Objects.requireNonNull(seatType, "seatType"));
    }

    public Map<SeatType, BigDecimal> asMap() {
        return Collections.unmodifiableMap(prices);
    }
}
