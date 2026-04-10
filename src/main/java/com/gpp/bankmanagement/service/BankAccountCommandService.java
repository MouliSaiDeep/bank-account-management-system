package com.gpp.bankmanagement.service;

import com.gpp.bankmanagement.domain.BankAccountState;
import com.gpp.bankmanagement.dto.CloseAccountRequest;
import com.gpp.bankmanagement.dto.CreateAccountRequest;
import com.gpp.bankmanagement.dto.DepositRequest;
import com.gpp.bankmanagement.dto.WithdrawRequest;
import com.gpp.bankmanagement.entity.BankAccountEventEntity;
import com.gpp.bankmanagement.entity.SnapshotEntity;
import com.gpp.bankmanagement.exception.BadRequestException;
import com.gpp.bankmanagement.exception.ConflictException;
import com.gpp.bankmanagement.exception.NotFoundException;
import com.gpp.bankmanagement.repository.BankAccountEventRepository;
import com.gpp.bankmanagement.repository.SnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class BankAccountCommandService {

    private static final String AGGREGATE_TYPE = "BankAccount";

    private final BankAccountEventRepository eventRepository;
    private final SnapshotRepository snapshotRepository;
    private final EventReplayService eventReplayService;
    private final ProjectionService projectionService;

    public BankAccountCommandService(BankAccountEventRepository eventRepository,
                                     SnapshotRepository snapshotRepository,
                                     EventReplayService eventReplayService,
                                     ProjectionService projectionService) {
        this.eventRepository = eventRepository;
        this.snapshotRepository = snapshotRepository;
        this.eventReplayService = eventReplayService;
        this.projectionService = projectionService;
    }

    @Transactional
    public void createAccount(CreateAccountRequest request) {
        if (eventRepository.existsByAggregateId(request.accountId())) {
            throw new ConflictException("Account already exists.");
        }
        if (request.initialBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Initial balance cannot be negative.");
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("accountId", request.accountId());
        eventData.put("ownerName", request.ownerName());
        eventData.put("initialBalance", request.initialBalance());
        eventData.put("currency", request.currency());
        eventData.put("status", "OPEN");

        BankAccountEventEntity event = new BankAccountEventEntity(
                UUID.randomUUID(),
                request.accountId(),
                AGGREGATE_TYPE,
                "AccountCreated",
                eventData,
                1,
                OffsetDateTime.now(ZoneOffset.UTC),
                1
        );

        eventRepository.save(event);
        long globalPosition = eventRepository.count();
        projectionService.projectEvent(event, globalPosition);

        BankAccountState state = BankAccountState.empty(request.accountId());
        state.apply(event);
        maybeSnapshot(request.accountId(), event.getEventNumber(), state);
    }

    @Transactional
    public void deposit(String accountId, DepositRequest request) {
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Deposit amount must be greater than zero.");
        }

        BankAccountState state = eventReplayService.loadCurrentState(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found."));

        var existingTransactionAccountId = eventRepository.findAggregateIdByTransactionId(request.transactionId());
        if (existingTransactionAccountId.isPresent()) {
            if (accountId.equals(existingTransactionAccountId.get())) {
                return;
            }
            throw new BadRequestException("Transaction ID already exists for another account.");
        }

        if (!state.isOpen()) {
            throw new ConflictException("Account is closed.");
        }

        int nextEventNumber = state.getLastEventNumber() + 1;
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("amount", request.amount());
        eventData.put("description", request.description());
        eventData.put("transactionId", request.transactionId());

        BankAccountEventEntity event = new BankAccountEventEntity(
                UUID.randomUUID(),
                accountId,
                AGGREGATE_TYPE,
                "MoneyDeposited",
                eventData,
                nextEventNumber,
                OffsetDateTime.now(ZoneOffset.UTC),
                1
        );

        eventRepository.save(event);
        long globalPosition = eventRepository.count();
        projectionService.projectEvent(event, globalPosition);
        state.apply(event);
        maybeSnapshot(accountId, nextEventNumber, state);
    }

    @Transactional
    public void withdraw(String accountId, WithdrawRequest request) {
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Withdraw amount must be greater than zero.");
        }

        BankAccountState state = eventReplayService.loadCurrentState(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found."));

        var existingTransactionAccountId = eventRepository.findAggregateIdByTransactionId(request.transactionId());
        if (existingTransactionAccountId.isPresent()) {
            if (accountId.equals(existingTransactionAccountId.get())) {
                return;
            }
            throw new BadRequestException("Transaction ID already exists for another account.");
        }

        if (!state.isOpen()) {
            throw new ConflictException("Account is closed.");
        }

        if (state.getBalance().compareTo(request.amount()) < 0) {
            throw new ConflictException("Insufficient funds.");
        }

        int nextEventNumber = state.getLastEventNumber() + 1;
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("amount", request.amount());
        eventData.put("description", request.description());
        eventData.put("transactionId", request.transactionId());

        BankAccountEventEntity event = new BankAccountEventEntity(
                UUID.randomUUID(),
                accountId,
                AGGREGATE_TYPE,
                "MoneyWithdrawn",
                eventData,
                nextEventNumber,
                OffsetDateTime.now(ZoneOffset.UTC),
                1
        );

        eventRepository.save(event);
        long globalPosition = eventRepository.count();
        projectionService.projectEvent(event, globalPosition);
        state.apply(event);
        maybeSnapshot(accountId, nextEventNumber, state);
    }

    @Transactional
    public void close(String accountId, CloseAccountRequest request) {
        BankAccountState state = eventReplayService.loadCurrentState(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found."));
        if (!state.isOpen()) {
            throw new ConflictException("Account is closed.");
        }
        if (state.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ConflictException("Account balance must be zero before closing.");
        }

        int nextEventNumber = state.getLastEventNumber() + 1;
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("reason", request.reason());

        BankAccountEventEntity event = new BankAccountEventEntity(
                UUID.randomUUID(),
                accountId,
                AGGREGATE_TYPE,
                "AccountClosed",
                eventData,
                nextEventNumber,
                OffsetDateTime.now(ZoneOffset.UTC),
                1
        );

        eventRepository.save(event);
        long globalPosition = eventRepository.count();
        projectionService.projectEvent(event, globalPosition);
        state.apply(event);
        maybeSnapshot(accountId, nextEventNumber, state);
    }

    private void maybeSnapshot(String accountId, int eventNumber, BankAccountState state) {
        if (eventNumber % 50 != 0) {
            return;
        }

        snapshotRepository.findFirstByAggregateIdOrderByLastEventNumberDesc(accountId)
                .ifPresent(snapshotRepository::delete);
        snapshotRepository.flush();

        SnapshotEntity snapshot = new SnapshotEntity(
                UUID.randomUUID(),
                accountId,
                state.toSnapshotData(),
                eventNumber,
                LocalDateTime.now(ZoneOffset.UTC)
        );
        snapshotRepository.save(snapshot);
    }
}