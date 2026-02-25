package com.massi.mvplogement.logement.dto;

import java.time.LocalDateTime;

public record LogementResponse(
        Long id,
        String title,
        String city,
        String description,
        LocalDateTime createdAt,
        Long ownerId,
        String ownerEmail
) {}