package com.hts.walletservice.dto.response;

import com.hts.walletservice.model.Wallet;

import java.util.List;

public record PagedResponse(
        List<Wallet> data,
        Integer      currentPage,
        Integer      pageSize,
        Long         totalItems,
        Integer      totalPages
) {
}
