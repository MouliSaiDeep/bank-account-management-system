package com.gpp.bankmanagement.controller;

import com.gpp.bankmanagement.dto.CloseAccountRequest;
import com.gpp.bankmanagement.dto.CreateAccountRequest;
import com.gpp.bankmanagement.dto.DepositRequest;
import com.gpp.bankmanagement.dto.WithdrawRequest;
import com.gpp.bankmanagement.service.BankAccountCommandService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountCommandController {

    private final BankAccountCommandService commandService;

    public AccountCommandController(BankAccountCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    public ResponseEntity<Void> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        commandService.createAccount(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<Void> deposit(@PathVariable String accountId, @Valid @RequestBody DepositRequest request) {
        commandService.deposit(accountId, request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<Void> withdraw(@PathVariable String accountId, @Valid @RequestBody WithdrawRequest request) {
        commandService.withdraw(accountId, request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{accountId}/close")
    public ResponseEntity<Void> close(@PathVariable String accountId, @Valid @RequestBody CloseAccountRequest request) {
        commandService.close(accountId, request);
        return ResponseEntity.accepted().build();
    }
}