CREATE TABLE IF NOT EXISTS transactions
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID           NOT NULL,
    type      VARCHAR(20)    NOT NULL,
    amount    DECIMAL(19, 4) NOT NULL,
    timestamp TIMESTAMPTZ    NOT NULL,

    FOREIGN KEY (wallet_id) REFERENCES wallets (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_transactions_wallet_type_timestamp ON transactions (wallet_id, type, timestamp);
