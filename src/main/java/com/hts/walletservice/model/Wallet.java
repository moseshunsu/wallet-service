package com.hts.walletservice.model;

import com.hts.walletservice.common.core.audit.AuditableData;
import com.querydsl.core.annotations.QueryEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Table("wallets")
@QueryEntity
@EqualsAndHashCode(callSuper = false)
public class Wallet extends AuditableData<Wallet> {

    @Id
    private UUID id;
    private String userId;
    private BigDecimal balance;

    public Wallet applyCreated(String userId) {
        this.userId    = userId;
        this.balance   = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();

        return this;
    }

}
