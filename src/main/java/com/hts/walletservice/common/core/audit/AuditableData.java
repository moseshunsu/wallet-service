package com.hts.walletservice.common.core.audit;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@SuppressWarnings("unchecked")
public abstract class AuditableData<T> implements Auditable<T> {

    /**
    The UUID's here represent the UUID's of the admins who performed the respective action
    **/
    protected Instant createdAt;
    protected UUID    createdBy;
    protected String  createdByUsername;

    protected Instant updatedAt;
    protected UUID    updatedBy;
    protected String  updatedByUsername;

    @Override
    public T setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return (T) this;
    }

    @Override
    public T setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
        return (T) this;
    }

    @Override
    public T setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
        return (T) this;
    }

    @Override
    public T setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return (T) this;
    }

    @Override
    public T setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
        return (T) this;
    }

    @Override
    public T setUpdatedByUsername(String updatedByUsername) {
        this.updatedByUsername = updatedByUsername;
        return (T) this;
    }

}
