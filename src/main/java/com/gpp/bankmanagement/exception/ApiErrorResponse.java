package com.gpp.bankmanagement.exception;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
        String message,
        int status,
        OffsetDateTime timestamp
) {
}