package com.gpp.bankmanagement.repository;

import com.gpp.bankmanagement.entity.BankAccountEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface BankAccountEventRepository extends JpaRepository<BankAccountEventEntity, UUID> {

    boolean existsByAggregateId(String aggregateId);

    List<BankAccountEventEntity> findByAggregateIdOrderByEventNumberAsc(String aggregateId);

    List<BankAccountEventEntity> findByAggregateIdAndEventNumberGreaterThanOrderByEventNumberAsc(String aggregateId, int eventNumber);

    List<BankAccountEventEntity> findByAggregateIdAndTimestampLessThanEqualOrderByEventNumberAsc(String aggregateId, OffsetDateTime timestamp);

    java.util.Optional<BankAccountEventEntity> findTopByAggregateIdOrderByEventNumberDesc(String aggregateId);
}