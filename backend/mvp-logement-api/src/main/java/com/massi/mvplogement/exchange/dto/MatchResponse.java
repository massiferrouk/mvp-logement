package com.massi.mvplogement.exchange.dto;

import java.time.LocalDate;

public record MatchResponse(
        Long exchangePeriodId,
        Long logementId,
        String haveCity,
        String wantCity,
        LocalDate startDate,
        LocalDate endDate,
        Long ownerId,
        String ownerEmail
) {}