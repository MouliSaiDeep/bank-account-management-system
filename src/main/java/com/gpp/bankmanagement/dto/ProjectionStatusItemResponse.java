package com.gpp.bankmanagement.dto;

public record ProjectionStatusItemResponse(
        String name,
        long lastProcessedEventNumberGlobal,
        long lag
) {
}