package com.hts.walletservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;

@FieldNameConstants
public record DepositMoneyRequest(

        @NotNull(message = "Amount cannot be null")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount

) {
}
