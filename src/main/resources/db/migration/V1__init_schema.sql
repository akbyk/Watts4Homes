CREATE TABLE homes (
                       id              BIGSERIAL PRIMARY KEY,
                       name            VARCHAR(255) NOT NULL,
                       address         VARCHAR(500),
                       contact_email   VARCHAR(255) NOT NULL,
                       budget_quota    NUMERIC(12,2) NOT NULL,
                       current_rate    NUMERIC(10,4) NOT NULL,
                       penalty_rate    NUMERIC(10,4) NOT NULL,
                       created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE appliances (
                            id               BIGSERIAL PRIMARY KEY,
                            home_id          BIGINT NOT NULL REFERENCES homes(id) ON DELETE CASCADE,
                            name             VARCHAR(255) NOT NULL,
                            type             VARCHAR(100) NOT NULL,
                            safe_limit_watts NUMERIC(10,2) NOT NULL
);

CREATE TABLE billing_ledger (
                                id                  BIGSERIAL PRIMARY KEY,
                                home_id             BIGINT NOT NULL REFERENCES homes(id) ON DELETE CASCADE,
                                accumulated_cost    NUMERIC(14,4) NOT NULL DEFAULT 0,
                                accumulated_usage   NUMERIC(14,4) NOT NULL DEFAULT 0,
                                tariff_state        VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
                                updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE event_log (
                           id          BIGSERIAL PRIMARY KEY,
                           home_id     BIGINT NOT NULL REFERENCES homes(id) ON DELETE CASCADE,
                           event_type  VARCHAR(50) NOT NULL, -- 80%_BREACH / 100%_BREACH / PENALTY_ACTIVATED / ANOMALY
                           timestamp   TIMESTAMPTZ NOT NULL DEFAULT now(),
                           metadata    JSONB
);

CREATE TABLE ai_recommendations (
                                    id              BIGSERIAL PRIMARY KEY,
                                    home_id         BIGINT NOT NULL REFERENCES homes(id) ON DELETE CASCADE,
                                    generated_text  TEXT NOT NULL,
                                    sent_at         TIMESTAMPTZ,
                                    email_status    VARCHAR(20) NOT NULL DEFAULT 'PENDING' -- PENDING / SENT / FAILED
);

CREATE TABLE consumption_snapshots (
                                       id           BIGSERIAL PRIMARY KEY,
                                       home_id      BIGINT NOT NULL REFERENCES homes(id) ON DELETE CASCADE,
                                       date         DATE NOT NULL,
                                       total_usage  NUMERIC(14,4) NOT NULL,
                                       total_cost   NUMERIC(14,4) NOT NULL,
                                       UNIQUE (home_id, date)
);

CREATE INDEX idx_appliances_home_id ON appliances(home_id);
CREATE INDEX idx_billing_ledger_home_id ON billing_ledger(home_id);
CREATE INDEX idx_event_log_home_id ON event_log(home_id);
CREATE INDEX idx_ai_recommendations_home_id ON ai_recommendations(home_id);
CREATE INDEX idx_consumption_snapshots_home_id ON consumption_snapshots(home_id);