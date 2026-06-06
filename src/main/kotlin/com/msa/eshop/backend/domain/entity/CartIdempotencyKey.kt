package com.msa.eshop.backend.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "cart_idempotency_keys",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_cart_idempotency_customer_endpoint_key",
            columnNames = ["customer_id", "endpoint", "idempotency_key"]
        )
    ],
    indexes = [
        Index(name = "idx_cart_idempotency_customer", columnList = "customer_id"),
        Index(name = "idx_cart_idempotency_key", columnList = "idempotency_key"),
        Index(name = "idx_cart_idempotency_cart", columnList = "cart_id")
    ]
)
open class CartIdempotencyKey(

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "customer_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_cart_idempotency_customer")
    )
    open var customer: Customer,

    @Column(name = "endpoint", nullable = false, length = 128)
    open var endpoint: String,

    @Column(name = "idempotency_key", nullable = false, length = 128)
    open var idempotencyKey: String,

    @Column(name = "request_hash", nullable = false, length = 128)
    open var requestHash: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "cart_id",
        foreignKey = ForeignKey(name = "fk_cart_idempotency_cart")
    )
    open var cart: Cart? = null,

    @Column(name = "status", nullable = false, length = 32)
    open var status: String = CartIdempotencyStatus.PROCESSING.name

) : AuditableUuidEntity() {

    fun markCompleted(cart: Cart) {
        this.cart = cart
        this.status = CartIdempotencyStatus.COMPLETED.name
    }

    fun markFailed() {
        this.status = CartIdempotencyStatus.FAILED.name
    }

    fun isCompleted(): Boolean =
        status == CartIdempotencyStatus.COMPLETED.name
}

enum class CartIdempotencyStatus {
    PROCESSING,
    COMPLETED,
    FAILED
}