package com.gpp.bankmanagement.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record EventResponse(
        String eventId,
        String eventType,
        int eventNumber,
        Map<String, Object> data,
        OffsetDateTime timestamp
) {
}