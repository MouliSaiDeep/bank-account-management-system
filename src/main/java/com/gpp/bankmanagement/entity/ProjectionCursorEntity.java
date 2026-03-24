package com.gpp.bankmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "projection_cursors")
public class ProjectionCursorEntity {

    @Id
    @Column(name = "projection_name", nullable = false, length = 100)
    private String projectionName;

    @Column(name = "last_processed_event_number_global", nullable = false)
    private Long lastProcessedEventNumberGlobal;

    protected ProjectionCursorEntity() {
    }

    public ProjectionCursorEntity(String projectionName, Long lastProcessedEventNumberGlobal) {
        this.projectionName = projectionName;
        this.lastProcessedEventNumberGlobal = lastProcessedEventNumberGlobal;
    }

    public String getProjectionName() {
        return projectionName;
    }

    public Long getLastProcessedEventNumberGlobal() {
        return lastProcessedEventNumberGlobal;
    }

    public void setLastProcessedEventNumberGlobal(Long lastProcessedEventNumberGlobal) {
        this.lastProcessedEventNumberGlobal = lastProcessedEventNumberGlobal;
    }
}