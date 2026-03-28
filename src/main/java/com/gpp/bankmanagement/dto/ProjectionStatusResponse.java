package com.gpp.bankmanagement.dto;

import java.util.List;

public record ProjectionStatusResponse(
        long totalEventsInStore,
        List<ProjectionStatusItemResponse> projections
) {
}