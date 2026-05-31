package com.msa.eshop.backend.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "refresh_tokens")
open class RefreshToken(

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    open var customer: Customer,

    @Column(name = "token_hash", nullable = false)
    open var tokenHash: String,

    @Column(name = "expires_at", nullable = false)
    open var expiresAt: OffsetDateTime,

    @Column(name = "revoked_at")
    open var revokedAt: OffsetDateTime? = null,

    @Column(name = "ip_address")
    open var ipAddress: String? = null,

    @Column(name = "user_agent")
    open var userAgent: String? = null

) : AuditableUuidEntity() {

    fun isActive(now: OffsetDateTime = OffsetDateTime.now()): Boolean {
        return revokedAt == null && expiresAt.isAfter(now)
    }

    fun revoke(now: OffsetDateTime = OffsetDateTime.now()) {
        revokedAt = now
    }
}