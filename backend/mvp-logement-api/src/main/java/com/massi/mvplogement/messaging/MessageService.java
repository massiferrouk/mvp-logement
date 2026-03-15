package com.massi.mvplogement.messaging;

import com.massi.mvplogement.messaging.dto.MessageResponse;
import com.massi.mvplogement.user.User;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationService conversationService;

    public MessageService(MessageRepository messageRepository,
                          ConversationService conversationService) {
        this.messageRepository = messageRepository;
        this.conversationService = conversationService;
    }

    @Transactional
    public MessageResponse sendMessage(Long conversationId,
                                       String content,
                                       Authentication auth) {

        Conversation conv = conversationService
                .getAuthorizedConversation(conversationId, auth);

        String email = auth.getName();

        User sender;

        if (conv.getExchangeRequest()
                .getFromPeriod()
                .getLogement()
                .getOwner()
                .getEmail()
                .equals(email)) {

            sender = conv.getExchangeRequest()
                    .getFromPeriod()
                    .getLogement()
                    .getOwner();
        } else {
            sender = conv.getExchangeRequest()
                    .getToPeriod()
                    .getLogement()
                    .getOwner();
        }

        Message msg = new Message();
        msg.setConversation(conv);
        msg.setSender(sender);
        msg.setContent(content);

        Message saved = messageRepository.save(msg);

        return new MessageResponse(
                saved.getId(),
                saved.getConversation().getId(),
                saved.getSender().getEmail(),
                saved.getContent(),
                saved.getCreatedAt()
        );
    }

    @Transactional
    public List<MessageResponse> getMessages(Long conversationId,
                                             Authentication auth) {

        Conversation conv = conversationService
                .getAuthorizedConversation(conversationId, auth);

        return messageRepository
                .findByConversation_IdOrderByCreatedAtAsc(conv.getId())
                .stream()
                .map(msg -> new MessageResponse(
                        msg.getId(),
                        msg.getConversation().getId(),
                        msg.getSender().getEmail(),
                        msg.getContent(),
                        msg.getCreatedAt()
                ))
                .toList();
    }
}