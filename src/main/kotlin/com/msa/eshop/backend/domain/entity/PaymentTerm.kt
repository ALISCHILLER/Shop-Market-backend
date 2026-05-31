package com.msa.eshop.backend.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Check

@Entity
@Table(
    name = "payment_terms",
    indexes = [
        Index(name = "idx_payment_terms_active_deadline", columnList = "active, dead_line")
    ]
)
@Check(
    constraints = "dead_line >= 0 and immediate_discount_percent >= 0 and immediate_discount_percent <= 100 and receipt_discount_percent >= 0 and receipt_discount_percent <= 100 and cheque_discount_percent >= 0 and cheque_discount_percent <= 100"
)
open class PaymentTerm(
    @Column(name = "name", nullable = false, length = 255)
    open var name: String = "",

    @Column(name = "dead_line", nullable = false)
    open var deadLine: Int = 0,

    @Column(name = "immediate_discount_percent", nullable = false)
    open var immediateDiscountPercent: Int = 0,

    @Column(name = "receipt_discount_percent", nullable = false)
    open var receiptDiscountPercent: Int = 0,

    @Column(name = "cheque_discount_percent", nullable = false)
    open var chequeDiscountPercent: Int = 0,

    @Column(name = "active", nullable = false)
    open var active: Boolean = true
) : AuditableUuidEntity(){
    fun isImmediate(): Boolean =
        deadLine == 0

    fun discountPercentFor(kind: PaymentKind): Int =
        when (kind) {
            PaymentKind.IMMEDIATE -> immediateDiscountPercent
            PaymentKind.RECEIPT -> receiptDiscountPercent
            PaymentKind.CHEQUE -> chequeDiscountPercent
        }

    fun activate() {
        active = true
    }

    fun deactivate() {
        active = false
    }
}