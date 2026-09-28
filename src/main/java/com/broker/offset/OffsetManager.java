package com.broker.offset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

public class OffsetManager {

    public static final Logger logger = LoggerFactory.getLogger(OffsetManager.class);
    private final HikariDataSource dataSource;

    public OffsetManager(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void saveOffset(String group, String topic, int partition, long offset) {
//         EXCLUDED is a special Postgres name for the row you tried {
//             to 
//         }insert in an INSERT ... ON CONFLICT DO UPDATE
//         .
// In:
//         INSERT 
//         INTO consumer_offsets(consumer_group, topic, partition_id, committed_offset)
//         VALUES( ?,  ?,  ?,  ?)
//         ON CONFLICT (consumer_group, topic, partition_id
//         )
// DO UPDATE SET committed_offset = EXCLUDED.committed_offset
//         the left side committed_offset is the existing row
//         ’s column (the one already in the table
//         )
// EXCLUDED.committed_offset is the new value 
//         from the VALUES(
//         ...) that conflicted
        String sql = """
            INSERT INTO consumer_offsets (consumer_group, topic, partition_id, committed_offset) VALUES (?, ?, ?, ?)
            ON CONFLICT (consumer_group, topic, partition_id)
            DO UPDATE SET committed_offset = EXCLUDED.committed_offset, updated_at = NOW()
        """;
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, group);
            ps.setString(2, topic);
            ps.setInt(3, partition);
            ps.setLong(4, offset);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Connection error: {}", e.getMessage());
            throw new RuntimeException("Failed to save offset for group=" + group
                    + " topic=" + topic
                    + " partition=" + partition, e);
        }
    }

    public long loadOffset(String group, String topic, int partition) {
        String sql = """
            SELECT committed_offset FROM consumer_offsets WHERE 
            consumer_group = ? AND topic = ? AND partition_id = ? 
        """;
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, group);
            ps.setString(2, topic);
            ps.setInt(3, partition);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("committed_offset");
                }
            }
        } catch (SQLException e) {
            logger.error("Connection error: {}", e.getMessage());
            throw new RuntimeException("Failed to get offset for group=" + group
                    + " topic=" + topic
                    + " partition=" + partition, e);
        }
        return 0;
    }

}
