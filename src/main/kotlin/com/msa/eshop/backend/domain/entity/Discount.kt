package com.msa.eshop.backend.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Check

@Entity
@Table(
    name = "discounts",
    indexes = [
        Index(name = "idx_discounts_product_id", columnList = "product_id"),
        Index(name = "idx_discounts_product_range", columnList = "product_id, from_number, end_number")
    ]
)
@Check(constraints = "discount_percent >= 0 and discount_percent <= 100 and from_number >= 1 and end_number >= from_number")
open class Discount(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_discounts_product")
    )
    open var product: Product? = null,

    @Column(name = "discount_percent", nullable = false)
    open var discountPercent: Int = 0,

    @Column(name = "from_number", nullable = false)
    open var fromNumber: Int = 1,

    @Column(name = "end_number", nullable = false)
    open var endNumber: Int = Int.MAX_VALUE
) : AuditableUuidEntity(){
    fun appliesTo(quantity: Int): Boolean =
        quantity in fromNumber..endNumber

    fun updateRange(
        fromNumber: Int,
        endNumber: Int
    ) {
        require(fromNumber >= 1) { "Discount range must start from 1 or more" }
        require(endNumber >= fromNumber) { "Discount end number must be greater than or equal to from number" }

        this.fromNumber = fromNumber
        this.endNumber = endNumber
    }

    fun updatePercent(percent: Int) {
        require(percent in 0..100) { "Discount percent must be between 0 and 100" }
        discountPercent = percent
    }
}