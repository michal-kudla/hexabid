-- Authorization schema: add audit columns to auctions and user_supervision table
-- Used by hexabid-adapter-out-db with Hibernate auto-ddl in local profile

ALTER TABLE auctions ADD COLUMN IF NOT EXISTS created_by_user_id VARCHAR(64) NOT NULL DEFAULT 'unknown';
ALTER TABLE auctions ADD COLUMN IF NOT EXISTS created_by_org_code VARCHAR(256) NOT NULL DEFAULT 'UNKNOWN';

CREATE TABLE IF NOT EXISTS user_supervision (
    manager_user_id    VARCHAR(64) NOT NULL,
    subordinate_user_id VARCHAR(64) NOT NULL,
    PRIMARY KEY (manager_user_id, subordinate_user_id)
);
