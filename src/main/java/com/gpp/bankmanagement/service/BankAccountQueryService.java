package com.gpp.bankmanagement.service;

import com.gpp.bankmanagement.domain.BankAccountState;
import com.gpp.bankmanagement.dto.AccountResponse;
import com.gpp.bankmanagement.dto.BalanceAtResponse;
import com.gpp.bankmanagement.dto.EventResponse;
import com.gpp.bankmanagement.dto.TransactionItemResponse;
import com.gpp.bankmanagement.dto.TransactionPageResponse;
import com.gpp.bankmanagement.entity.BankAccountEventEntity;
import com.gpp.bankmanagement.exception.NotFoundException;
import com.gpp.bankmanagement.repository.BankAccountEventRepository;
import com.gpp.bankmanagement.repository.AccountSummaryRepository;
import com.gpp.bankmanagement.repository.TransactionHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class BankAccountQueryService {

    private final AccountSummaryRepository accountSummaryRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final BankAccountEventRepository eventRepository;
    private final EventReplayService eventReplayService;

    public BankAccountQueryService(AccountSummaryRepository accountSummaryRepository,
                                   TransactionHistoryRepository transactionHistoryRepository,
                                   BankAccountEventRepository eventRepository,
                                   EventReplayService eventReplayService) {
        this.accountSummaryRepository = accountSummaryRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.eventRepository = eventRepository;
        this.eventReplayService = eventReplayService;
    }

    public AccountResponse getAccount(String accountId) {
        return accountSummaryRepository.findById(accountId)
                .map(summary -> new AccountResponse(summary.getAccountId(), summary.getOwnerName(), summary.getBalance(), summary.getCurrency(), summary.getStatus()))
                .orElseThrow(() -> new NotFoundException("Account not found."));
    }

    public List<EventResponse> getEvents(String accountId) {
        if (!eventRepository.existsByAggregateId(accountId)) {
            throw new NotFoundException("Account not found.");
        }
        return eventRepository.findByAggregateIdOrderByEventNumberAsc(accountId)
                .stream()
                .map(this::toEventResponse)
                .toList();
    }

    public BalanceAtResponse getBalanceAt(String accountId, OffsetDateTime timestamp) {
        BankAccountState state = eventReplayService.loadStateAt(accountId, timestamp)
                .orElseThrow(() -> new NotFoundException("Account not found."));
        return new BalanceAtResponse(accountId, state.getBalance(), timestamp);
    }

    public TransactionPageResponse getTransactions(String accountId, int page, int pageSize) {
        if (!accountSummaryRepository.existsById(accountId)) {
            throw new NotFoundException("Account not found.");
        }

        var pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "timestamp"));
        var results = transactionHistoryRepository.findByAccountIdOrderByTimestampAsc(accountId, pageable);
        List<TransactionItemResponse> items = results.getContent().stream()
                .map(transaction -> new TransactionItemResponse(
                        transaction.getTransactionId(),
                        transaction.getType(),
                        transaction.getAmount(),
                        transaction.getDescription(),
                        transaction.getTimestamp()))
                .toList();

        int totalPages = results.getTotalElements() == 0 ? 0 : (int) Math.ceil((double) results.getTotalElements() / pageSize);
        return new TransactionPageResponse(page, pageSize, totalPages, results.getTotalElements(), items);
    }

    private EventResponse toEventResponse(BankAccountEventEntity event) {
        return new EventResponse(event.getEventId().toString(), event.getEventType(), event.getEventNumber(), event.getEventData(), event.getTimestamp());
    }
}