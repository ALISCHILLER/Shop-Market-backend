package com.msa.eshop.backend.domain.repository

import com.msa.eshop.backend.domain.entity.ProductCategory
import org.springframework.data.jpa.repository.JpaRepository

interface ProductCategoryRepository : JpaRepository<ProductCategory, Int> {

    fun findAllByOrderByProductCategoryCodeAsc(): List<ProductCategory>
}