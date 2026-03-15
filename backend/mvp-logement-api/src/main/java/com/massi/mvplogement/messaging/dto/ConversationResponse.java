package com.massi.mvplogement.messaging.dto;

import java.time.LocalDateTime;

public record ConversationResponse(
        Long id,
        Long exchangeRequestId,
        String otherUserEmail,
        LocalDateTime createdAt
) {
}