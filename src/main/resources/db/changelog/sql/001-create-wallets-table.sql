CREATE TABLE IF NOT EXISTS wallets
(
    id                  UUID PRIMARY KEY,
    user_id             VARCHAR(255) NOT NULL UNIQUE,
    balance             DECIMAL(19, 4) DEFAULT 0 NOT NULL,

    -- auditable --
    created_at          TIMESTAMPTZ NOT NULL,
    created_by          UUID,
    created_by_username VARCHAR(255),
    updated_at          TIMESTAMPTZ NOT NULL,
    updated_by          UUID,
    updated_by_username VARCHAR(255)
);

/**
  This is just for knowledge’s sake, Since user_id is already UNIQUE,
  PostgreSQL automatically creates an index for it. So this explicit index creation below is redundant.
  Also, an automatic index will also be created for id since it's also unique

  CREATE INDEX idx_wallets_user_id ON wallets(user_id)
**/