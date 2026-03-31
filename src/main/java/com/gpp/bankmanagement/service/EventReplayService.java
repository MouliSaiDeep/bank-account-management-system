package com.gpp.bankmanagement.service;

import com.gpp.bankmanagement.domain.BankAccountState;
import com.gpp.bankmanagement.entity.BankAccountEventEntity;
import com.gpp.bankmanagement.entity.SnapshotEntity;
import com.gpp.bankmanagement.repository.BankAccountEventRepository;
import com.gpp.bankmanagement.repository.SnapshotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
public class EventReplayService {

    private final BankAccountEventRepository eventRepository;
    private final SnapshotRepository snapshotRepository;

    public EventReplayService(BankAccountEventRepository eventRepository, SnapshotRepository snapshotRepository) {
        this.eventRepository = eventRepository;
        this.snapshotRepository = snapshotRepository;
    }

    public Optional<BankAccountState> loadCurrentState(String accountId) {
        Optional<SnapshotEntity> snapshot = snapshotRepository.findFirstByAggregateIdOrderByLastEventNumberDesc(accountId);
        return loadFromSnapshot(accountId, snapshot, null);
    }

    public Optional<BankAccountState> loadStateAt(String accountId, OffsetDateTime timestamp) {
        Optional<SnapshotEntity> snapshot = snapshotRepository.findFirstByAggregateIdAndCreatedAtLessThanEqualOrderByLastEventNumberDesc(accountId, timestamp.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
        return loadFromSnapshot(accountId, snapshot, timestamp);
    }

    private Optional<BankAccountState> loadFromSnapshot(String accountId, Optional<SnapshotEntity> snapshot, OffsetDateTime timestamp) {
        BankAccountState state;
        int snapshotLastEventNumber = 0;

        if (snapshot.isPresent()) {
            state = BankAccountState.fromSnapshot(snapshot.get().getSnapshotData());
            snapshotLastEventNumber = snapshot.get().getLastEventNumber();
        } else {
            state = BankAccountState.empty(accountId);
        }

        List<BankAccountEventEntity> events;
        if (timestamp == null) {
            events = snapshotLastEventNumber == 0
                    ? eventRepository.findByAggregateIdOrderByEventNumberAsc(accountId)
                    : eventRepository.findByAggregateIdAndEventNumberGreaterThanOrderByEventNumberAsc(accountId, snapshotLastEventNumber);
        } else {
            events = eventRepository.findByAggregateIdAndTimestampLessThanEqualOrderByEventNumberAsc(accountId, timestamp);
            if (snapshotLastEventNumber > 0) {
                final int finalSnapshotLastEventNumber = snapshotLastEventNumber;
                events = events.stream().filter(event -> event.getEventNumber() > finalSnapshotLastEventNumber).toList();
            }
        }

        for (BankAccountEventEntity event : events) {
            state.apply(event);
        }

        if (!state.exists()) {
            return Optional.empty();
        }

        return Optional.of(state);
    }
}