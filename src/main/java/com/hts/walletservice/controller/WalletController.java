package com.hts.walletservice.controller;

import com.hts.walletservice.dto.request.CreateWalletRequest;
import com.hts.walletservice.model.Wallet;
import com.hts.walletservice.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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

}
