docker run -d \
  --name broker-postgres \
  -e POSTGRES_DB=broker_db \
  -e POSTGRES_USER=broker \
  -e POSTGRES_PASSWORD=broker123 \
  -p 5432:5432 \
  postgres:16

docker exec -i broker-postgres psql -U broker -d broker_db -c "
CREATE TABLE IF NOT EXISTS consumer_offsets (
    consumer_group  VARCHAR(100) NOT NULL,
    topic           VARCHAR(100) NOT NULL,
    partition_id    INT NOT NULL,
    committed_offset BIGINT NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (consumer_group, topic, partition_id)
);"
