package com.massi.mvplogement.messaging.dto;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long conversationId,
        String senderEmail,
        String content,
        LocalDateTime createdAt
) {
}