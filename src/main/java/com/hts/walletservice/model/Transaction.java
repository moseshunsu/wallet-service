package com.hts.walletservice.model;

import com.querydsl.core.annotations.QueryEntity;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Table("transactions")
@QueryEntity
public class Transaction {

    @Id
    private UUID id;
    private UUID walletId;
    private Type type;
    private BigDecimal amount;
    private Instant timestamp;

    public Transaction applyCreated(Wallet wallet, Type type, BigDecimal amount, Instant now) {
        this.walletId = wallet.getId();
        this.type = type;
        this.amount = amount;
        this.timestamp = now;

        return this;
    }

}
