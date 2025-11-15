package com.hts.walletservice.common.core.audit;

import java.time.Instant;
import java.util.UUID;

public interface AuditableCreated<T> {

    Instant getCreatedAt();
    UUID    getCreatedBy();
    String  getCreatedByUsername();

    T setCreatedBy(UUID createdBy);
    T setCreatedAt(Instant createdAt);
    T setCreatedByUsername(String createdByUsername);

}
