CREATE TABLE IF NOT EXISTS consumer_offsets (
    consumer_group  VARCHAR(100) NOT NULL,
    topic           VARCHAR(100) NOT NULL,
    partition_id    INT NOT NULL,
    committed_offset BIGINT NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (consumer_group, topic, partition_id)
);