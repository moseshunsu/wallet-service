package com.hts.walletservice.controller;

import com.hts.walletservice.dto.request.CreateWalletRequest;
import com.hts.walletservice.dto.request.DepositMoneyRequest;
import com.hts.walletservice.model.Wallet;
import com.hts.walletservice.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Wallet> createWallet(@RequestBody @Valid CreateWalletRequest request) {
        return walletService.createWallet(request.userId());
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Wallet> getWallet(@PathVariable String userId) {
        return walletService.getWallet(userId);
    }

    @GetMapping
    public Flux<Wallet> readCollection() {
        return walletService.readCollection();
    }

    @PostMapping("{userId}/deposit")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Wallet> depositMoney(@PathVariable String userId, @RequestBody @Valid DepositMoneyRequest request) {
        return walletService.depositMoney(userId, request.amount());
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteWallet(@PathVariable String userId) {
        return walletService.deleteWallet(userId);
    }

}
