package com.hts.walletservice.service.impl;

import com.hts.walletservice.common.cache.WalletCache;
import com.hts.walletservice.dto.response.PagedResponse;
import com.hts.walletservice.model.Transaction;
import com.hts.walletservice.model.Type;
import com.hts.walletservice.model.Wallet;
import com.hts.walletservice.repository.TransactionRepository;
import com.hts.walletservice.repository.WalletRepository;
import com.hts.walletservice.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static com.hts.walletservice.model.Type.DEPOSIT;
import static com.hts.walletservice.model.Type.WITHDRAWAL;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final Clock clock;
    private final WalletCache walletCache;

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
                    Wallet wallet = new Wallet().applyCreated(userId, clock.instant());
                    return walletRepository.save(wallet);
                }));
    }

    @Override
    public Mono<Wallet> getWallet(String userId) {
        return walletCache.get(userId)
                .switchIfEmpty(walletRepository.findByUserId(userId)
                        .switchIfEmpty(Mono.error(notFound(userId)))
                        .flatMap(walletCache::set)
                );
    }

    @Override
    public Flux<Wallet> readCollection() {
        return walletRepository.findAll();
    }

    @Override
    public Mono<PagedResponse> readCollection(Integer pageNumber, Integer size) {
        return walletRepository.findAllWithPagination(size, (pageNumber - 1) * size)
                .collectList()
                .zipWith(countAllWallets())
                .map(tuple -> {
                    List<Wallet> wallets = tuple.getT1();
                    Long totalItems = tuple.getT2();
                    int totalPages = (int) Math.ceil((double) totalItems / size);

                    return new PagedResponse(
                            wallets,
                            pageNumber,
                            size,
                            totalItems,
                            totalPages
                    );
                });
    }

    @Override
    public Mono<Wallet> depositMoney(String userId, BigDecimal amount) {
        return walletRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(notFound(userId)))
                .flatMap(this::getSumDeposits)
                .flatMap(tuple -> validateDepositLimit(tuple, amount))
                .flatMap(wallet -> updateWallet(wallet, amount))
                .flatMap(savedWallet -> createTransaction(savedWallet, DEPOSIT, amount, clock.instant())
                        .thenReturn(savedWallet))
                .flatMap(savedWallet -> walletCache.remove(savedWallet.getUserId()).thenReturn(savedWallet));
    }

    @Override
    public Mono<Void> deleteWallet(String userId) {
        return walletRepository.deleteByUserId(userId)
                .flatMap(count -> count > 1
                        ? Mono.empty()
                        : Mono.error(notFound(userId))
                );
    }

    @Override
    public Mono<Wallet> withdrawMoney(String userId, BigDecimal amount) {
        return walletRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(notFound(userId)))
                .filter(wallet -> validateWithdrawalAmount(wallet, amount))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient Amount!!!")))
                .flatMap(wallet -> {
                    wallet.setBalance(wallet.getBalance().subtract(amount));
                    wallet.setUpdatedAt(clock.instant());
                    return walletRepository.save(wallet);
                })
                .flatMap(savedWallet -> createTransaction(savedWallet, WITHDRAWAL, amount, clock.instant())
                        .thenReturn(savedWallet))
                .flatMap(savedWallet -> walletCache.remove(savedWallet.getUserId()).thenReturn(savedWallet));
    }

    private Mono<Tuple2<Wallet, BigDecimal>> getSumDeposits(Wallet wallet) {
        return transactionRepository.findAllByWalletIdAndTypeAndTimestampAfter(
                        wallet.getId(),
                        DEPOSIT,
                        clock.instant().minus(Duration.ofHours(24))
                )
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .map(sumDeposits -> Tuples.of(wallet, sumDeposits));
    }

    private Mono<Wallet> validateDepositLimit(Tuple2<Wallet, BigDecimal> tuple, BigDecimal amount) {
        var wallet = tuple.getT1();
        var currentDayDeposits = tuple.getT2();
        var totalIncludingNewDeposit = currentDayDeposits.add(amount);
        var dailyLimit = wallet.getDailyDepositLimit();

        if (totalIncludingNewDeposit.compareTo(dailyLimit) > 0) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Deposit limit exceeded. Limit: %s, Current: %s, Attempted: %s",
                            dailyLimit, currentDayDeposits, amount)));
        }

        return Mono.just(wallet);
    }

    private Mono<Wallet> updateWallet(Wallet wallet, BigDecimal amount) {
        var balance = wallet.getBalance().add(amount);
        wallet.setBalance(balance);
        wallet.setUpdatedAt(clock.instant());
        return walletRepository.save(wallet);
    }

    private Mono<Transaction> createTransaction(Wallet wallet, Type type, BigDecimal amount, Instant now) {
        var transaction = new Transaction().applyCreated(wallet, type, amount, now);
        return transactionRepository.save(transaction);
    }

    private Boolean validateWithdrawalAmount(Wallet wallet, BigDecimal amount) {
        return wallet.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) >= 0;
    }

    private ResponseStatusException notFound(String userId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No wallet found for userId: " + userId);
    }

    private Mono<Long> countAllWallets() {
        return walletRepository.count();
    }

}
