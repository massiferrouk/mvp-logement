package com.massi.mvplogement.exchange.dto;

import jakarta.validation.constraints.NotNull;

public record CreateExchangeRequestRequest(
        @NotNull Long fromPeriodId,
        @NotNull Long toPeriodId
) {}