package com.massi.mvplogement.messaging.events;

import com.massi.mvplogement.messaging.Message;
import org.springframework.context.ApplicationEvent;

public class MessageSentEvent extends ApplicationEvent {

    private final Message message;

    public MessageSentEvent(Object source, Message message) {
        super(source);
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}

