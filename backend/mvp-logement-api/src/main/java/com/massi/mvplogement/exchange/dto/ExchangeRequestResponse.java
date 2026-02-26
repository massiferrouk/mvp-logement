package com.massi.mvplogement.exchange.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExchangeRequestResponse(
        Long id,
        String status,
        LocalDateTime createdAt,

        Long fromPeriodId,
        Long fromLogementId,
        String fromHaveCity,
        String fromWantCity,
        LocalDate fromStartDate,
        LocalDate fromEndDate,
        String fromOwnerEmail,

        Long toPeriodId,
        Long toLogementId,
        String toHaveCity,
        String toWantCity,
        LocalDate toStartDate,
        LocalDate toEndDate,
        String toOwnerEmail
) {}