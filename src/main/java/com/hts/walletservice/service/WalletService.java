package com.hts.walletservice.service;

import com.hts.walletservice.model.Wallet;
import reactor.core.publisher.Mono;

public interface WalletService {

    Mono<Wallet> createWallet(String userId);

}
