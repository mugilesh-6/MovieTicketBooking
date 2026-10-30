package com.movieticketbooking.member4.service;

import com.movieticketbooking.member4.model.Offer;
import com.movieticketbooking.member4.repository.OfferRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

public class OfferService {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private final OfferRepository offerRepository;

    public OfferService(OfferRepository offerRepository) {
        this.offerRepository = Objects.requireNonNull(offerRepository);
    }

    public void createOffer(Offer offer) {
        offerRepository.save(offer);
    }

    public Offer validateOffer(String code, String userId, BigDecimal bookingAmount, LocalDateTime now) {
        Offer offer = offerRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid offer code"));
        if (now.isBefore(offer.validFrom()) || now.isAfter(offer.validUntil())) {
            throw new IllegalArgumentException("Offer is not currently valid");
        }
        if (bookingAmount.compareTo(offer.minimumBookingAmount()) < 0) {
            throw new IllegalArgumentException("Booking does not meet the offer minimum amount");
        }
        if (offerRepository.hasUserUsedOffer(userId, offer.code())) {
            throw new IllegalArgumentException("This user has already used the offer");
        }
        return offer;
    }

    public BigDecimal calculateDiscount(Offer offer, BigDecimal eligibleAmount) {
        BigDecimal percentageDiscount = eligibleAmount.multiply(offer.discountPercentage())
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        return percentageDiscount.min(offer.maximumDiscount()).min(eligibleAmount)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void markUsed(String userId, String offerCode) {
        offerRepository.markOfferUsed(userId, offerCode);
    }
}