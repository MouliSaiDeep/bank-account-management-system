package com.gpp.bankmanagement.dto;

import java.math.BigDecimal;

public record AccountResponse(
        String accountId,
        String ownerName,
        BigDecimal balance,
        String currency,
        String status
) {
}