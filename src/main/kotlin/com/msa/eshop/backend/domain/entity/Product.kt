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
import org.hibernate.annotations.Check

@Entity
@Table(
    name = "products",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_products_product_code",
            columnNames = ["product_code"]
        )
    ],
    indexes = [
        Index(name = "idx_products_group_code", columnList = "product_group_code"),
        Index(name = "idx_products_product_name", columnList = "product_name"),
        Index(name = "idx_products_is_discounts", columnList = "is_discounts"),
        Index(name = "idx_products_is_tax", columnList = "is_tax")
    ]
)
@Check(constraints = "price >= 0 and convert_factor1 > 0 and convert_factor2 > 0")
open class Product(
    @Column(name = "product_name", length = 255)
    open var productName: String? = null,

    @Column(name = "product_code", nullable = false)
    open var productCode: Int = 0,

    @Column(name = "full_name_kala1", length = 255)
    open var fullNameKala1: String? = null,

    @Column(name = "unit1", length = 64)
    open var unit1: String? = null,

    @Column(name = "unitid1", length = 64)
    open var unitid1: String? = null,

    @Column(name = "convert_factor1", nullable = false)
    open var convertFactor1: Int = 1,

    @Column(name = "full_name_kala2", length = 255)
    open var fullNameKala2: String? = null,

    @Column(name = "unit2", length = 64)
    open var unit2: String? = null,

    @Column(name = "convert_factor2", nullable = false)
    open var convertFactor2: Int = 1,

    @Column(name = "unitid2", length = 64)
    open var unitid2: String? = null,

    @Column(name = "product_group_code", nullable = false)
    open var productGroupCode: Int = 0,

    @Column(name = "price", nullable = false)
    open var price: Long = 0,

    @Column(name = "is_discounts", nullable = false)
    open var isDiscounts: Boolean = false,

    @Column(name = "is_tax", nullable = false)
    open var isTax: Boolean = true,

    @Column(name = "product_image", columnDefinition = "text")
    open var productImage: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_group_code",
        referencedColumnName = "product_category_code",
        insertable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_products_product_category")
    )
    open var category: ProductCategory? = null
) : AuditableUuidEntity()