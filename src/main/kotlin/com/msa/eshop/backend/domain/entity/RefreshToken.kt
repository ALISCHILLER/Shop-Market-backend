package com.msa.eshop.backend.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "refresh_tokens",
    indexes = [
        Index(name = "idx_refresh_tokens_customer", columnList = "customer_id"),
        Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at"),
        Index(name = "idx_refresh_tokens_customer_active", columnList = "customer_id, revoked_at, expires_at"),
        Index(name = "idx_refresh_tokens_family", columnList = "family_id"),
        Index(name = "idx_refresh_tokens_reuse_detected", columnList = "reuse_detected_at")
    ]
)
open class RefreshToken(

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    open var customer: Customer,

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    open var tokenHash: String,

    @Column(name = "family_id", nullable = false)
    open var familyId: UUID = UUID.randomUUID(),

    @Column(name = "replaced_by_token_hash", length = 255)
    open var replacedByTokenHash: String? = null,

    @Column(name = "expires_at", nullable = false)
    open var expiresAt: OffsetDateTime,

    @Column(name = "revoked_at")
    open var revokedAt: OffsetDateTime? = null,

    @Column(name = "reuse_detected_at")
    open var reuseDetectedAt: OffsetDateTime? = null,

    @Column(name = "ip_address", length = 64)
    open var ipAddress: String? = null,

    @Column(name = "user_agent", columnDefinition = "text")
    open var userAgent: String? = null

) : AuditableUuidEntity() {

    fun isActive(now: OffsetDateTime = OffsetDateTime.now()): Boolean =
        revokedAt == null && expiresAt.isAfter(now)

    fun revoke(now: OffsetDateTime = OffsetDateTime.now()) {
        revokedAt = now
    }

    fun markReused(now: OffsetDateTime = OffsetDateTime.now()) {
        reuseDetectedAt = now
        revokedAt = revokedAt ?: now
    }

    fun markReplacedBy(newTokenHash: String, now: OffsetDateTime = OffsetDateTime.now()) {
        replacedByTokenHash = newTokenHash
        revoke(now)
    }
}