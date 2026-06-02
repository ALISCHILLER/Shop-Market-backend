package com.msa.eshop.backend.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "audit_logs")
open class AuditLog(

    @Column(name = "actor_customer_id")
    open var actorCustomerId: UUID? = null,

    @Column(name = "actor_customer_code", length = 64)
    open var actorCustomerCode: String? = null,

    @Column(name = "actor_customer_name", length = 255)
    open var actorCustomerName: String? = null,

    @Column(name = "action", nullable = false, length = 128)
    open var action: String,

    @Column(name = "entity_type", nullable = false, length = 128)
    open var entityType: String,

    @Column(name = "entity_id", length = 128)
    open var entityId: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", columnDefinition = "jsonb")
    open var oldValue: Map<String, Any?>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", columnDefinition = "jsonb")
    open var newValue: Map<String, Any?>? = null,

    @Column(name = "ip_address", length = 64)
    open var ipAddress: String? = null,

    @Column(name = "user_agent", columnDefinition = "text")
    open var userAgent: String? = null,

    @Column(name = "description", columnDefinition = "text")
    open var description: String? = null

) : BaseUuidEntity() {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    open var createdAt: OffsetDateTime = OffsetDateTime.now()
}