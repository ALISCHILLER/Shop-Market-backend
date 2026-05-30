package com.msa.eshop.backend.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "product_categories",
    indexes = [
        Index(name = "idx_product_categories_name", columnList = "product_category_name")
    ]
)
open class ProductCategory(
    @Id
    @Column(name = "product_category_code", nullable = false)
    open var productCategoryCode: Int = 0,

    @Column(name = "product_category_name", length = 255)
    open var productCategoryName: String? = null,

    @Column(name = "product_category_image", columnDefinition = "text")
    open var productCategoryImage: String? = null,

    @Column(name = "product_category_image_unselect", columnDefinition = "text")
    open var productCategoryImageUnselect: String? = null
)