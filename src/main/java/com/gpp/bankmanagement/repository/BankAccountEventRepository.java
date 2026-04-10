package com.gpp.bankmanagement.repository;

import com.gpp.bankmanagement.entity.BankAccountEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankAccountEventRepository extends JpaRepository<BankAccountEventEntity, UUID> {

    boolean existsByAggregateId(String aggregateId);

    List<BankAccountEventEntity> findByAggregateIdOrderByEventNumberAsc(String aggregateId);

    List<BankAccountEventEntity> findByAggregateIdAndEventNumberGreaterThanOrderByEventNumberAsc(String aggregateId, int eventNumber);

    List<BankAccountEventEntity> findByAggregateIdAndTimestampLessThanEqualOrderByEventNumberAsc(String aggregateId, OffsetDateTime timestamp);

        Optional<BankAccountEventEntity> findTopByAggregateIdOrderByEventNumberDesc(String aggregateId);

        @Query(value = """
                        SELECT e.aggregate_id
                        FROM events e
                        WHERE e.event_type IN ('MoneyDeposited', 'MoneyWithdrawn')
                            AND e.event_data ->> 'transactionId' = :transactionId
                        LIMIT 1
                        """, nativeQuery = true)
        Optional<String> findAggregateIdByTransactionId(@Param("transactionId") String transactionId);
}