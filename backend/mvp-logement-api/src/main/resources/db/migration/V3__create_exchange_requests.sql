CREATE TABLE IF NOT EXISTS exchange_requests (
                                   id BIGSERIAL PRIMARY KEY,

                                   from_period_id BIGINT NOT NULL REFERENCES exchange_periods(id) ON DELETE CASCADE,
                                   to_period_id   BIGINT NOT NULL REFERENCES exchange_periods(id) ON DELETE CASCADE,

                                   status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT uq_exchange_request_pair UNIQUE (from_period_id, to_period_id),
                                   CONSTRAINT chk_exchange_request_status CHECK (status IN ('PENDING','ACCEPTED','REJECTED','CANCELED'))
);

CREATE INDEX IF NOT EXISTS idx_exchange_requests_from_period ON exchange_requests(from_period_id);
CREATE INDEX IF NOT EXISTS idx_exchange_requests_to_period ON exchange_requests(to_period_id);
CREATE INDEX IF NOT EXISTS idx_exchange_requests_status ON exchange_requests(status);