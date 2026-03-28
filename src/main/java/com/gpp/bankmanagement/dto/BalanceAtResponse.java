package com.gpp.bankmanagement.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BalanceAtResponse(
        String accountId,
        BigDecimal balanceAt,
        OffsetDateTime timestamp
) {
}