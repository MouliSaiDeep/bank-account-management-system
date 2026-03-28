package com.gpp.bankmanagement.dto;

import jakarta.validation.constraints.NotBlank;

public record CloseAccountRequest(
        @NotBlank String reason
) {
}