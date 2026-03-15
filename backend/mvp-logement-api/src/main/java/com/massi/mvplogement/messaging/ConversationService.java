package com.massi.mvplogement.messaging;

import com.massi.mvplogement.common.ForbiddenException;
import com.massi.mvplogement.common.NotFoundException;
import com.massi.mvplogement.messaging.dto.ConversationResponse;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Transactional
    public Conversation getAuthorizedConversation(Long id, Authentication auth) {
        String email = auth.getName();

        Conversation conv = conversationRepository.findDetailedById(id)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        String ownerA = conv.getExchangeRequest()
                .getFromPeriod()
                .getLogement()
                .getOwner()
                .getEmail();

        String ownerB = conv.getExchangeRequest()
                .getToPeriod()
                .getLogement()
                .getOwner()
                .getEmail();

        if (!email.equals(ownerA) && !email.equals(ownerB)) {
            throw new ForbiddenException("Access denied");
        }

        return conv;
    }

    @Transactional
    public List<ConversationResponse> getMyConversations(Authentication auth) {
        String email = auth.getName();

        return conversationRepository.findAllDetailed().stream()
                .filter(conv ->
                        conv.getExchangeRequest()
                                .getFromPeriod()
                                .getLogement()
                                .getOwner()
                                .getEmail()
                                .equals(email)
                                ||
                                conv.getExchangeRequest()
                                        .getToPeriod()
                                        .getLogement()
                                        .getOwner()
                                        .getEmail()
                                        .equals(email)
                )
                .map(conv -> toResponse(conv, email))
                .toList();
    }

    @Transactional
    public ConversationResponse getConversationResponse(Long id, Authentication auth) {
        Conversation conv = getAuthorizedConversation(id, auth);
        return toResponse(conv, auth.getName());
    }

    private ConversationResponse toResponse(Conversation conv, String currentEmail) {
        String ownerA = conv.getExchangeRequest()
                .getFromPeriod()
                .getLogement()
                .getOwner()
                .getEmail();

        String ownerB = conv.getExchangeRequest()
                .getToPeriod()
                .getLogement()
                .getOwner()
                .getEmail();

        String otherUserEmail = currentEmail.equals(ownerA) ? ownerB : ownerA;

        return new ConversationResponse(
                conv.getId(),
                conv.getExchangeRequest().getId(),
                otherUserEmail,
                conv.getCreatedAt()
        );
    }
}