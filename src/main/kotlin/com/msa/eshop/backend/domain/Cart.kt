package com.msa.eshop.backend.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Check
import java.time.LocalDate

@Entity
@Table(
    name = "carts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_carts_cart_code",
            columnNames = ["cart_code"]
        )
    ],
    indexes = [
        Index(name = "idx_carts_customer_date", columnList = "customer_id, sales_date"),
        Index(name = "idx_carts_status_code", columnList = "status_code"),
        Index(name = "idx_carts_status_date", columnList = "status_code, sales_date"),
        Index(name = "idx_carts_created_at", columnList = "created_at")
    ]
)
@Check(constraints = "subtotal >= 0 and discount_total >= 0 and tax_total >= 0 and total >= 0")
open class Cart(
    @Column(name = "cart_code", nullable = false)
    open var cartCode: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "customer_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_carts_customer")
    )
    open var customer: Customer? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "customer_address_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_carts_customer_address")
    )
    open var address: CustomerAddress? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "payment_term_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_carts_payment_term")
    )
    open var paymentTerm: PaymentTerm? = null,

    @Column(name = "customer_name_snapshot", nullable = false, length = 255)
    open var customerNameSnapshot: String = "",

    @Column(name = "customer_address_snapshot", nullable = false, columnDefinition = "text")
    open var customerAddressSnapshot: String = "",

    @Column(name = "status_code", nullable = false, length = 32)
    open var statusCode: String = CartStatus.REGISTERED.code,

    @Column(name = "status_name", nullable = false, length = 128)
    open var statusName: String = CartStatus.REGISTERED.title,

    @Column(name = "status_color", nullable = false, length = 32)
    open var statusColor: String = CartStatus.REGISTERED.color,

    @Column(name = "sales_date", nullable = false)
    open var salesDate: LocalDate = LocalDate.now(),

    @Column(name = "subtotal", nullable = false)
    open var subtotal: Long = 0,

    @Column(name = "discount_total", nullable = false)
    open var discountTotal: Long = 0,

    @Column(name = "tax_total", nullable = false)
    open var taxTotal: Long = 0,

    @Column(name = "total", nullable = false)
    open var total: Long = 0,

    @OneToMany(
        mappedBy = "cart",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    open var items: MutableList<CartItem> = mutableListOf()
) : AuditableUuidEntity() {
    fun addItem(item: CartItem) {
        item.cart = this
        items.add(item)
    }

    fun applyStatus(
        status: CartStatus,
        customColor: String? = null
    ) {
        statusCode = status.code
        statusName = status.title
        statusColor = customColor?.takeIf { it.isNotBlank() } ?: status.color
    }
}