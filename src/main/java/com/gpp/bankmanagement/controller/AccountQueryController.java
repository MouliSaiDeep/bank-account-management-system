package com.gpp.bankmanagement.controller;

import com.gpp.bankmanagement.dto.AccountResponse;
import com.gpp.bankmanagement.dto.BalanceAtResponse;
import com.gpp.bankmanagement.dto.EventResponse;
import com.gpp.bankmanagement.dto.TransactionPageResponse;
import com.gpp.bankmanagement.service.BankAccountQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountQueryController {

    private final BankAccountQueryService queryService;

    public AccountQueryController(BankAccountQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{accountId}")
    public AccountResponse getAccount(@PathVariable String accountId) {
        return queryService.getAccount(accountId);
    }

    @GetMapping("/{accountId}/events")
    public List<EventResponse> getEvents(@PathVariable String accountId) {
        return queryService.getEvents(accountId);
    }

    @GetMapping("/{accountId}/balance-at/{timestamp}")
    public BalanceAtResponse getBalanceAt(@PathVariable String accountId, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime timestamp) {
        return queryService.getBalanceAt(accountId, timestamp);
    }

    @GetMapping("/{accountId}/transactions")
    public TransactionPageResponse getTransactions(@PathVariable String accountId,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return queryService.getTransactions(accountId, page, pageSize);
    }
}