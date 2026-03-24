package com.gpp.bankmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "snapshots",
        uniqueConstraints = @UniqueConstraint(name = "uk_snapshots_aggregate_id", columnNames = {"aggregate_id"}),
        indexes = @Index(name = "idx_snapshots_aggregate_id", columnList = "aggregate_id")
)
public class SnapshotEntity {

    @Id
    @Column(name = "snapshot_id", nullable = false, updatable = false)
    private UUID snapshotId;

    @Column(name = "aggregate_id", nullable = false, length = 255, unique = true)
    private String aggregateId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> snapshotData;

    @Column(name = "last_event_number", nullable = false)
    private int lastEventNumber;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected SnapshotEntity() {
    }

    public SnapshotEntity(UUID snapshotId, String aggregateId, Map<String, Object> snapshotData, int lastEventNumber, LocalDateTime createdAt) {
        this.snapshotId = snapshotId;
        this.aggregateId = aggregateId;
        this.snapshotData = snapshotData;
        this.lastEventNumber = lastEventNumber;
        this.createdAt = createdAt;
    }

    public UUID getSnapshotId() {
        return snapshotId;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public Map<String, Object> getSnapshotData() {
        return snapshotData;
    }

    public int getLastEventNumber() {
        return lastEventNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}