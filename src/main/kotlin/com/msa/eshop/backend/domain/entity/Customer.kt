package com.msa.eshop.backend.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Check
import java.time.OffsetDateTime

@Entity
@Table(
    name = "customers",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_customers_customer_code",
            columnNames = ["customer_code"]
        )
    ],
    indexes = [
        Index(name = "idx_customers_role", columnList = "role"),
        Index(name = "idx_customers_enabled", columnList = "enabled"),
        Index(name = "idx_customers_created_at", columnList = "created_at"),
        Index(name = "idx_customers_token_version", columnList = "token_version")
    ]
)
@Check(constraints = "role in ('ADMIN', 'CUSTOMER')")
open class Customer(
    @Column(name = "customer_code", nullable = false, length = 64)
    open var customerCode: String = "",

    @Column(name = "customer_name", nullable = false, length = 255)
    open var customerName: String = "",

    @Column(name = "mobile", length = 32)
    open var mobile: String? = null,

    @Column(name = "phone", length = 32)
    open var phone: String? = null,

    @Column(name = "center", length = 255)
    open var center: String? = null,

    @Column(name = "national_code", length = 32)
    open var nationalCode: String? = null,

    @Column(name = "password_hash", nullable = false, length = 255)
    open var passwordHash: String = "",

    @Column(name = "salt", length = 255)
    open var salt: String? = null,

    @Column(name = "password_change_required", nullable = false)
    open var passwordChangeRequired: Boolean = false,

    @Column(name = "password_changed_at")
    open var passwordChangedAt: OffsetDateTime? = null,

    @Column(name = "token_version", nullable = false)
    open var tokenVersion: Long = 0,

    @Column(name = "role", nullable = false, length = 32)
    open var role: String = CustomerRole.CUSTOMER.name,

    @Column(name = "enabled", nullable = false)
    open var enabled: Boolean = true
) : AuditableUuidEntity() {

    fun applyRole(role: CustomerRole) {
        this.role = role.name
    }

    fun isAdmin(): Boolean =
        CustomerRole.normalize(role) == CustomerRole.ADMIN

    fun enable() {
        enabled = true
    }

    fun disable() {
        enabled = false
        invalidateTokens()
    }

    fun markPasswordChangeRequired() {
        passwordChangeRequired = true
    }

    fun clearPasswordChangeRequired() {
        passwordChangeRequired = false
    }

    fun changePasswordHash(
        encodedPassword: String,
        algorithm: String = "bcrypt",
        requireChange: Boolean
    ) {
        passwordHash = encodedPassword
        salt = algorithm
        passwordChangeRequired = requireChange
        passwordChangedAt = OffsetDateTime.now()
        invalidateTokens()
    }

    fun invalidateTokens() {
        tokenVersion += 1
    }

    fun updateBasicInfo(
        customerCode: String,
        customerName: String,
        mobile: String?,
        phone: String?,
        center: String?,
        nationalCode: String?
    ) {
        this.customerCode = customerCode
        this.customerName = customerName
        this.mobile = mobile
        this.phone = phone
        this.center = center
        this.nationalCode = nationalCode
    }
}