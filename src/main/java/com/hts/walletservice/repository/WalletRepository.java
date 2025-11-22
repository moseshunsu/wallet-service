package com.hts.walletservice.repository;

import com.hts.walletservice.model.Wallet;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WalletRepository extends R2dbcRepository<Wallet, UUID> {

    Mono<Wallet> findByUserId(String userId);

    Mono<Integer> deleteByUserId(String userId);

}
