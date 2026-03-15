package com.massi.mvplogement.messaging.events;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MessageNotificationListener {

    @EventListener
    public void onMessageSent(MessageSentEvent event) {
        // Hook extensible : envoi d'e-mail, websocket, logs, etc.
        // Pour l'instant, on ne fait rien de plus que réagir à l'événement.
    }
}

