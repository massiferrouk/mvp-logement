package com.massi.mvplogement.messaging;

import com.massi.mvplogement.messaging.dto.ConversationResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping("/me")
    public List<ConversationResponse> myConversations(Authentication auth) {
        return conversationService.getMyConversations(auth);
    }

    @GetMapping("/{id}")
    public ConversationResponse getConversation(@PathVariable Long id,
                                                Authentication auth) {
        return conversationService.getConversationResponse(id, auth);
    }
}