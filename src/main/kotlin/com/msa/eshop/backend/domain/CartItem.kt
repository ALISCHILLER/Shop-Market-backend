package com.msa.eshop.backend.domain

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
    name = "cart_items",
    indexes = [
        Index(name = "idx_cart_items_cart_id", columnList = "cart_id"),
        Index(name = "idx_cart_items_product_id", columnList = "product_id"),
        Index(name = "idx_cart_items_product_code", columnList = "product_code")
    ]
)
@Check(constraints = "quantity > 0 and price >= 0 and discount >= 0 and tax >= 0 and total >= 0")
open class CartItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "cart_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_cart_items_cart")
    )
    open var cart: Cart? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_cart_items_product")
    )
    open var product: Product? = null,

    @Column(name = "product_code", nullable = false)
    open var productCode: Int = 0,

    @Column(name = "product_name", nullable = false, length = 255)
    open var productName: String = "",

    @Column(name = "product_image_url", columnDefinition = "text")
    open var productImageUrl: String? = null,

    @Column(name = "quantity", nullable = false)
    open var quantity: Int = 0,

    @Column(name = "price", nullable = false)
    open var price: Int = 0,

    @Column(name = "discount", nullable = false)
    open var discount: Int = 0,

    @Column(name = "tax", nullable = false)
    open var tax: Int = 0,

    @Column(name = "total", nullable = false)
    open var total: Int = 0
) : AuditableUuidEntity()