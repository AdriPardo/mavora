package com.mavora.audit.domain;

public interface AuditEventRepository {

    void append(AuditEvent event);
}
