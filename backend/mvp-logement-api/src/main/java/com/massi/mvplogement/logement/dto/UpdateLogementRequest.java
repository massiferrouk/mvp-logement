package com.massi.mvplogement.logement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLogementRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 100) String city,
        String description
) {}