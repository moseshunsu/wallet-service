package com.hts.walletservice.common.cache;

import com.hts.walletservice.model.Wallet;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Service
public class WalletCache {

    private static final Long TTL = 30L;
    private static final String KEY_FORMAT = "wallet:%s";
    private static final String WALLET_CACHE_NAME = "wallets-cache";

    private final RMapCacheReactive<String, Wallet> cache;

    public WalletCache(RedissonReactiveClient redissonClient) {
        this.cache = redissonClient.getMapCache(WALLET_CACHE_NAME);
    }

    public Mono<Wallet> set(Wallet wallet) {
        return cache.fastPut(buildKey(wallet.getUserId()), wallet, TTL, TimeUnit.SECONDS)
                .thenReturn(wallet);
    }

    public Mono<Wallet> get(String userId) {
        return cache.get(buildKey(userId))
                .onErrorResume(err -> Mono.empty());
    }

    public Mono<Void> remove(String userId) {
        return cache.fastRemove(buildKey(userId))
                .then();
    }

    private String buildKey(String userId) {
        return String.format(KEY_FORMAT, userId);
    }

}
