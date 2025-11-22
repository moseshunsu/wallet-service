package com.hts.walletservice.service.impl;

import com.hts.walletservice.model.Wallet;
import com.hts.walletservice.repository.WalletRepository;
import com.hts.walletservice.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

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
                        Mono.<Wallet>error(new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Wallet already exists for user: " + userId))
                )
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("No existing wallet found, creating new wallet for userId: {}", userId);
                    Wallet wallet = new Wallet().applyCreated(userId);
                    return walletRepository.save(wallet);
                }));
    }

    @Override
    public Mono<Wallet> getWallet(String userId) {
        return walletRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(notFound(userId)));
    }

    @Override
    public Flux<Wallet> readCollection() {
        return walletRepository.findAll();
    }

    @Override
    public Mono<Wallet> depositMoney(String userId, BigDecimal amount) {
        return walletRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(notFound(userId)))
                .flatMap(wallet -> {
                    var balance = wallet.getBalance().add(amount);
                    wallet.setBalance(balance);
                    wallet.setUpdatedAt(Instant.now());
                    return walletRepository.save(wallet);
                });
    }

    @Override
    public Mono<Void> deleteWallet(String userId) {
        return walletRepository.deleteByUserId(userId)
                .flatMap(count -> count > 1
                        ? Mono.empty()
                        : Mono.error(notFound(userId))
                );
    }

    private ResponseStatusException notFound(String userId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No wallet found for userId: " + userId);
    }

}
