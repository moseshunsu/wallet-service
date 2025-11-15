package com.hts.walletservice.common.core.audit;

import java.time.Instant;
import java.util.UUID;

public interface AuditableUpdated<T> {

    Instant getUpdatedAt();
    UUID    getUpdatedBy();
    String  getUpdatedByUsername();

    T setUpdatedAt(Instant updatedAt);
    T setUpdatedBy(UUID updatedBy);
    T setUpdatedByUsername(String updatedByUsername);

}
