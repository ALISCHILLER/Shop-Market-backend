package com.msa.eshop.backend.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "stock_reservations",
    indexes = [
        Index(name = "idx_stock_reservations_cart", columnList = "cart_id"),
        Index(name = "idx_stock_reservations_product", columnList = "product_id"),
        Index(name = "idx_stock_reservations_status", columnList = "status")
    ]
)
open class StockReservation(

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "cart_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_stock_reservations_cart")
    )
    open var cart: Cart,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_stock_reservations_product")
    )
    open var product: Product,

    @Column(name = "product_code", nullable = false)
    open var productCode: Int,

    @Column(name = "quantity", nullable = false)
    open var quantity: Int,

    @Column(name = "status", nullable = false, length = 32)
    open var status: String = StockReservationStatus.ACTIVE.name

) : AuditableUuidEntity() {

    fun release() {
        status = StockReservationStatus.RELEASED.name
    }

    fun consume() {
        status = StockReservationStatus.CONSUMED.name
    }

    fun isActive(): Boolean =
        status == StockReservationStatus.ACTIVE.name
}

enum class StockReservationStatus {
    ACTIVE,
    RELEASED,
    CONSUMED
}