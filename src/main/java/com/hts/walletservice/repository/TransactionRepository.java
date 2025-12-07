package com.hts.walletservice.repository;

import com.hts.walletservice.model.Transaction;
import com.hts.walletservice.model.Type;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.UUID;

public interface TransactionRepository extends R2dbcRepository<Transaction, UUID> {
    Flux<Transaction> findAllByWalletIdAndTypeAndTimestampAfter(UUID id, Type type, Instant instant);
}
