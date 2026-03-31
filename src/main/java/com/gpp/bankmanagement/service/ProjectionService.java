package com.gpp.bankmanagement.service;

import com.gpp.bankmanagement.domain.AccountStatus;
import com.gpp.bankmanagement.entity.AccountSummaryEntity;
import com.gpp.bankmanagement.entity.BankAccountEventEntity;
import com.gpp.bankmanagement.entity.ProjectionCursorEntity;
import com.gpp.bankmanagement.entity.TransactionHistoryEntity;
import com.gpp.bankmanagement.dto.ProjectionStatusItemResponse;
import com.gpp.bankmanagement.dto.ProjectionStatusResponse;
import com.gpp.bankmanagement.repository.AccountSummaryRepository;
import com.gpp.bankmanagement.repository.BankAccountEventRepository;
import com.gpp.bankmanagement.repository.ProjectionCursorRepository;
import com.gpp.bankmanagement.repository.TransactionHistoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class ProjectionService {

    private static final String ACCOUNT_SUMMARIES = "AccountSummaries";
    private static final String TRANSACTION_HISTORY = "TransactionHistory";

    private final AccountSummaryRepository accountSummaryRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ProjectionCursorRepository projectionCursorRepository;
    private final BankAccountEventRepository eventRepository;

    public ProjectionService(AccountSummaryRepository accountSummaryRepository,
                             TransactionHistoryRepository transactionHistoryRepository,
                             ProjectionCursorRepository projectionCursorRepository,
                             BankAccountEventRepository eventRepository) {
        this.accountSummaryRepository = accountSummaryRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.projectionCursorRepository = projectionCursorRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public void projectEvent(BankAccountEventEntity event, long globalPosition) {
        applyAccountSummary(event, globalPosition);
        applyTransactionHistory(event, globalPosition);
        updateCursor(ACCOUNT_SUMMARIES, globalPosition);
        updateCursor(TRANSACTION_HISTORY, globalPosition);
    }

    @Transactional
    public void rebuild() {
        transactionHistoryRepository.deleteAllInBatch();
        accountSummaryRepository.deleteAllInBatch();
        projectionCursorRepository.deleteAllInBatch();

        List<BankAccountEventEntity> events = eventRepository.findAll(Sort.by(Sort.Order.asc("timestamp"), Sort.Order.asc("eventNumber"), Sort.Order.asc("eventId")));
        long globalPosition = 0;
        for (BankAccountEventEntity event : events) {
            globalPosition++;
            applyAccountSummary(event, globalPosition);
            applyTransactionHistory(event, globalPosition);
            updateCursor(ACCOUNT_SUMMARIES, globalPosition);
            updateCursor(TRANSACTION_HISTORY, globalPosition);
        }
    }

    @Transactional(readOnly = true)
    public ProjectionStatusResponse getStatus() {
        long totalEvents = eventRepository.count();
        long accountSummaryPosition = projectionCursorRepository.findById(ACCOUNT_SUMMARIES)
                .map(ProjectionCursorEntity::getLastProcessedEventNumberGlobal)
                .orElse(0L);
        long transactionHistoryPosition = projectionCursorRepository.findById(TRANSACTION_HISTORY)
                .map(ProjectionCursorEntity::getLastProcessedEventNumberGlobal)
                .orElse(0L);

        return new ProjectionStatusResponse(
                totalEvents,
                List.of(
                        new ProjectionStatusItemResponse(ACCOUNT_SUMMARIES, accountSummaryPosition, Math.max(0, totalEvents - accountSummaryPosition)),
                        new ProjectionStatusItemResponse(TRANSACTION_HISTORY, transactionHistoryPosition, Math.max(0, totalEvents - transactionHistoryPosition))
                )
        );
    }

    private void applyAccountSummary(BankAccountEventEntity event, long globalPosition) {
        Map<String, Object> data = event.getEventData();
        switch (event.getEventType()) {
            case "AccountCreated" -> {
                AccountSummaryEntity summary = new AccountSummaryEntity(
                        event.getAggregateId(),
                        stringValue(data.get("ownerName")),
                        decimalValue(data.get("initialBalance")),
                        stringValue(data.get("currency")),
                        AccountStatus.OPEN.name(),
                        (long) event.getEventNumber()
                );
                accountSummaryRepository.save(summary);
            }
            case "MoneyDeposited" -> upsertBalance(event, globalPosition, decimalValue(data.get("amount")), false);
            case "MoneyWithdrawn" -> upsertBalance(event, globalPosition, decimalValue(data.get("amount")), true);
            case "AccountClosed" -> upsertClose(event, globalPosition);
            default -> throw new IllegalStateException("Unsupported event type: " + event.getEventType());
        }
    }

    private void upsertBalance(BankAccountEventEntity event, long globalPosition, BigDecimal amount, boolean subtract) {
        accountSummaryRepository.findById(event.getAggregateId()).ifPresent(summary -> {
            if (summary.getVersion() != null && summary.getVersion() >= event.getEventNumber()) {
                return;
            }
            BigDecimal updatedBalance = subtract ? summary.getBalance().subtract(amount) : summary.getBalance().add(amount);
            summary.update(summary.getOwnerName(), updatedBalance, summary.getCurrency(), summary.getStatus(), (long) event.getEventNumber());
            accountSummaryRepository.save(summary);
        });
    }

    private void upsertClose(BankAccountEventEntity event, long globalPosition) {
        accountSummaryRepository.findById(event.getAggregateId()).ifPresent(summary -> {
            if (summary.getVersion() != null && summary.getVersion() >= event.getEventNumber()) {
                return;
            }
            summary.update(summary.getOwnerName(), summary.getBalance(), summary.getCurrency(), AccountStatus.CLOSED.name(), (long) event.getEventNumber());
            accountSummaryRepository.save(summary);
        });
    }

    private void applyTransactionHistory(BankAccountEventEntity event, long globalPosition) {
        Map<String, Object> data = event.getEventData();
        if ("MoneyDeposited".equals(event.getEventType()) || "MoneyWithdrawn".equals(event.getEventType())) {
            String transactionId = stringValue(data.get("transactionId"));
            if (!transactionHistoryRepository.existsById(transactionId)) {
                transactionHistoryRepository.save(new TransactionHistoryEntity(
                        transactionId,
                        event.getAggregateId(),
                        "MoneyDeposited".equals(event.getEventType()) ? "DEPOSIT" : "WITHDRAWAL",
                        decimalValue(data.get("amount")),
                        stringValue(data.get("description")),
                        event.getTimestamp()
                ));
            }
        }
    }

    private void updateCursor(String projectionName, long globalPosition) {
        ProjectionCursorEntity cursor = projectionCursorRepository.findById(projectionName)
                .orElseGet(() -> new ProjectionCursorEntity(projectionName, 0L));
        cursor.setLastProcessedEventNumberGlobal(globalPosition);
        projectionCursorRepository.save(cursor);
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static BigDecimal decimalValue(Object value) {
        return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
    }
}