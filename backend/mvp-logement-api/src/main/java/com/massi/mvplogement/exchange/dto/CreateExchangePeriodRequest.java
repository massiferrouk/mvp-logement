package com.massi.mvplogement.exchange.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateExchangePeriodRequest(
        @NotNull Long logementId,
        @NotBlank @Size(max = 100) String wantCity,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}