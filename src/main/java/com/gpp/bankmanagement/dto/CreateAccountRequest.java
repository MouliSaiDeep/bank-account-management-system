package com.gpp.bankmanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank String accountId,
        @NotBlank String ownerName,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal initialBalance,
        @NotBlank String currency
) {
}