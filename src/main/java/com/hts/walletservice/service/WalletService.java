package com.hts.walletservice.service;

import com.hts.walletservice.dto.response.PagedResponse;
import com.hts.walletservice.model.Wallet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface WalletService {

    Mono<Wallet> createWallet(String userId);

    Mono<Wallet> getWallet(String userId);

    Flux<Wallet> readCollection();

    Mono<PagedResponse> readCollection(Integer pageNumber, Integer size);

    Mono<Wallet> depositMoney(String userId, BigDecimal amount);

    Mono<Void> deleteWallet(String userId);

    Mono<Wallet> withdrawMoney(String userId,  BigDecimal amount);
}
