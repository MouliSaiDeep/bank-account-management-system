package com.gpp.bankmanagement.domain;

import com.gpp.bankmanagement.entity.BankAccountEventEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BankAccountState {

    private final String accountId;
    private String ownerName;
    private BigDecimal balance = BigDecimal.ZERO;
    private String currency;
    private AccountStatus status;
    private int lastEventNumber;
    private final Set<String> processedTransactionIds = new HashSet<>();
    private boolean exists;

    private BankAccountState(String accountId) {
        this.accountId = accountId;
    }

    public static BankAccountState empty(String accountId) {
        return new BankAccountState(accountId);
    }

    public static BankAccountState fromSnapshot(Map<String, Object> snapshotData) {
        BankAccountState state = new BankAccountState(stringValue(snapshotData.get("accountId")));
        state.ownerName = stringValue(snapshotData.get("ownerName"));
        state.balance = decimalValue(snapshotData.get("balance"));
        state.currency = stringValue(snapshotData.get("currency"));
        state.status = AccountStatus.valueOf(stringValue(snapshotData.get("status")));
        state.lastEventNumber = integerValue(snapshotData.get("lastEventNumber"));
        state.exists = true;

        Object transactionIds = snapshotData.get("processedTransactionIds");
        if (transactionIds instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (item != null) {
                    state.processedTransactionIds.add(item.toString());
                }
            }
        }

        return state;
    }

    public void apply(BankAccountEventEntity event) {
        Map<String, Object> data = event.getEventData();
        switch (event.getEventType()) {
            case "AccountCreated" -> {
                this.ownerName = stringValue(data.get("ownerName"));
                this.balance = decimalValue(data.get("initialBalance"));
                this.currency = stringValue(data.get("currency"));
                this.status = AccountStatus.OPEN;
                this.exists = true;
            }
            case "MoneyDeposited" -> {
                this.balance = this.balance.add(decimalValue(data.get("amount")));
                addTransactionId(stringValue(data.get("transactionId")));
            }
            case "MoneyWithdrawn" -> {
                this.balance = this.balance.subtract(decimalValue(data.get("amount")));
                addTransactionId(stringValue(data.get("transactionId")));
            }
            case "AccountClosed" -> this.status = AccountStatus.CLOSED;
            default -> throw new IllegalStateException("Unsupported event type: " + event.getEventType());
        }
        this.lastEventNumber = event.getEventNumber();
    }

    public Map<String, Object> toSnapshotData() {
        Map<String, Object> snapshotData = new HashMap<>();
        snapshotData.put("accountId", accountId);
        snapshotData.put("ownerName", ownerName);
        snapshotData.put("balance", balance.toPlainString());
        snapshotData.put("currency", currency);
        snapshotData.put("status", status.name());
        snapshotData.put("lastEventNumber", lastEventNumber);
        snapshotData.put("processedTransactionIds", new ArrayList<>(processedTransactionIds));
        return snapshotData;
    }

    public boolean hasTransactionId(String transactionId) {
        return processedTransactionIds.contains(transactionId);
    }

    public void addTransactionId(String transactionId) {
        if (transactionId != null && !transactionId.isBlank()) {
            processedTransactionIds.add(transactionId);
        }
    }

    public String getAccountId() {
        return accountId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public int getLastEventNumber() {
        return lastEventNumber;
    }

    public boolean exists() {
        return exists;
    }

    public boolean isOpen() {
        return exists && status == AccountStatus.OPEN;
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static BigDecimal decimalValue(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }

    private static int integerValue(Object value) {
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value.toString());
    }
}