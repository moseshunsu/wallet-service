package com.hts.walletservice.common.core.audit;

public interface Auditable<T> extends AuditableCreated<T>, AuditableUpdated<T> {
}
