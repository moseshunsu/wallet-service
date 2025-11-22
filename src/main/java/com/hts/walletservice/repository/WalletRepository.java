package com.hts.walletservice.repository;

import com.hts.walletservice.model.Wallet;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WalletRepository extends R2dbcRepository<Wallet, UUID> {

    Mono<Wallet> findByUserId(String userId);

    Mono<Integer> deleteByUserId(String userId);

    @Query("SELECT * FROM wallets LIMIT :limit OFFSET :offset")
    Flux<Wallet> findAllWithPagination(int limit, int offset);

}
