package com.massi.mvplogement.messaging;

import com.massi.mvplogement.messaging.dto.MessageResponse;
import com.massi.mvplogement.messaging.dto.SendMessageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/{id}/messages")
    public List<MessageResponse> getMessages(@PathVariable Long id,
                                             Authentication auth) {
        return messageService.getMessages(id, auth);
    }

    @PostMapping("/{id}/messages")
    public MessageResponse sendMessage(@PathVariable Long id,
                                       @RequestBody SendMessageRequest req,
                                       Authentication auth) {
        return messageService.sendMessage(id, req.content(), auth);
    }
}