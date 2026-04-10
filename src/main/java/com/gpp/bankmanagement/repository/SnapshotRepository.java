package com.gpp.bankmanagement.repository;

import com.gpp.bankmanagement.entity.SnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SnapshotRepository extends JpaRepository<SnapshotEntity, UUID> {

    Optional<SnapshotEntity> findFirstByAggregateIdOrderByLastEventNumberDesc(String aggregateId);

    Optional<SnapshotEntity> findFirstByAggregateIdAndLastEventNumberLessThanEqualOrderByLastEventNumberDesc(String aggregateId, int lastEventNumber);
}