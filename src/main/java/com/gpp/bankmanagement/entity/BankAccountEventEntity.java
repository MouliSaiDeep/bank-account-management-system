package com.gpp.bankmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "events",
        uniqueConstraints = @UniqueConstraint(name = "uk_events_aggregate_id_event_number", columnNames = {"aggregate_id", "event_number"}),
        indexes = @Index(name = "idx_events_aggregate_id", columnList = "aggregate_id")
)
public class BankAccountEventEntity {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "aggregate_id", nullable = false, length = 255)
    private String aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 255)
    private String aggregateType;

    @Column(name = "event_type", nullable = false, length = 255)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> eventData;

    @Column(name = "event_number", nullable = false)
    private int eventNumber;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    @Column(name = "version", nullable = false)
    private int version = 1;

    protected BankAccountEventEntity() {
    }

    public BankAccountEventEntity(UUID eventId, String aggregateId, String aggregateType, String eventType, Map<String, Object> eventData, int eventNumber, OffsetDateTime timestamp, int version) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.eventData = eventData;
        this.eventNumber = eventNumber;
        this.timestamp = timestamp;
        this.version = version;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getEventType() {
        return eventType;
    }

    public Map<String, Object> getEventData() {
        return eventData;
    }

    public int getEventNumber() {
        return eventNumber;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public int getVersion() {
        return version;
    }
}