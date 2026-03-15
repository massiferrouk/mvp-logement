CREATE TABLE IF NOT EXISTS conversations (
                               id BIGSERIAL PRIMARY KEY,
                               exchange_request_id BIGINT UNIQUE NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_conversation_exchange_request
                                   FOREIGN KEY (exchange_request_id)
                                       REFERENCES exchange_requests(id)
                                       ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS messages (
                          id BIGSERIAL PRIMARY KEY,
                          conversation_id BIGINT NOT NULL,
                          sender_id BIGINT NOT NULL,
                          content TEXT NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_message_conversation
                              FOREIGN KEY (conversation_id)
                                  REFERENCES conversations(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_message_sender
                              FOREIGN KEY (sender_id)
                                  REFERENCES users(id)
                                  ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_conversations_exchange_request_id
    ON conversations(exchange_request_id);

CREATE INDEX IF NOT EXISTS idx_messages_conversation_id
    ON messages(conversation_id);

CREATE INDEX IF NOT EXISTS idx_messages_sender_id
    ON messages(sender_id);

CREATE INDEX IF NOT EXISTS idx_messages_created_at
    ON messages(created_at);