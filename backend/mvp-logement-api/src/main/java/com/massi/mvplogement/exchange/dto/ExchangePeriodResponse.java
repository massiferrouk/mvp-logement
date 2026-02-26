package com.massi.mvplogement.exchange.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExchangePeriodResponse(
        Long id,
        Long logementId,
        String haveCity,
        String wantCity,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        LocalDateTime createdAt
) {}