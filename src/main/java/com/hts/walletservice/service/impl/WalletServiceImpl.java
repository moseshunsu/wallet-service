package com.hts.walletservice.service.impl;

import com.hts.walletservice.model.Wallet;
import com.hts.walletservice.repository.WalletRepository;
import com.hts.walletservice.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Override
    public Mono<Wallet> createWallet(String userId) {
        log.info("Creating wallet for userId: {}", userId);

        return walletRepository.findByUserId(userId)
                .flatMap(existing ->
                        Mono.<Wallet>error(new RuntimeException("Wallet already exists for user: " + userId))
                )
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("No existing wallet found, creating new wallet for userId: {}", userId);
                    Wallet wallet = new Wallet().applyCreated(userId);
                    return walletRepository.save(wallet);
                }));
    }

}
