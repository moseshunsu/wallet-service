package com.hts.walletservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public record CreateWalletRequest(

        @NotBlank(message = "UserId cannot be blank")
        String userId

) {
}
