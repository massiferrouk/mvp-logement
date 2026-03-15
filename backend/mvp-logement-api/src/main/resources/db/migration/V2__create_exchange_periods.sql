CREATE TABLE IF NOT EXISTS exchange_periods (
                                  id BIGSERIAL PRIMARY KEY,
                                  logement_id BIGINT NOT NULL REFERENCES logements(id) ON DELETE CASCADE,

    -- ville où l'utilisateur veut aller
                                  want_city VARCHAR(100) NOT NULL,

                                  start_date DATE NOT NULL,
                                  end_date DATE NOT NULL,

                                  status VARCHAR(20) NOT NULL DEFAULT 'OPEN',

                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT chk_exchange_dates CHECK (start_date <= end_date)
);

CREATE INDEX IF NOT EXISTS idx_exchange_periods_logement_id ON exchange_periods(logement_id);
CREATE INDEX IF NOT EXISTS idx_exchange_periods_want_city ON exchange_periods(want_city);
CREATE INDEX IF NOT EXISTS idx_exchange_periods_date_range ON exchange_periods(start_date, end_date);