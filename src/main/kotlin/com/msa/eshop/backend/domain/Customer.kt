package com.msa.eshop.backend.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Check

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
        Index(name = "idx_customers_created_at", columnList = "created_at")
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
}