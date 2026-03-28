package com.gpp.bankmanagement.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionItemResponse(
        String transactionId,
        String type,
        BigDecimal amount,
        String description,
        OffsetDateTime timestamp
) {
}