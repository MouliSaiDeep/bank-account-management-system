package com.gpp.bankmanagement.dto;

import java.util.List;

public record TransactionPageResponse(
        int currentPage,
        int pageSize,
        int totalPages,
        long totalCount,
        List<TransactionItemResponse> items
) {
}