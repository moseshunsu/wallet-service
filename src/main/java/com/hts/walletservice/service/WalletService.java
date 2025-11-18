package com.hts.walletservice.service;

import com.hts.walletservice.model.Wallet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WalletService {

    Mono<Wallet> createWallet(String userId);

    Mono<Wallet> getWallet(String userId);

    Flux<Wallet> readCollection();
}
