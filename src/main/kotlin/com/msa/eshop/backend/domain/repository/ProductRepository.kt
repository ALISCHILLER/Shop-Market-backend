package com.msa.eshop.backend.domain.repository

import com.msa.eshop.backend.domain.entity.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
interface ProductRepository : JpaRepository<Product, UUID> {

    fun findByProductCode(productCode: Int): Product?

    fun findByProductCodeIn(productCodes: Collection<Int>): List<Product>

    fun existsByProductCode(productCode: Int): Boolean

    fun existsByProductCodeAndIdNot(
        productCode: Int,
        id: UUID
    ): Boolean

    fun countByProductGroupCode(productGroupCode: Int): Long

    fun findAllByOrderByProductNameAsc(): List<Product>

    fun findByProductGroupCodeOrderByProductNameAsc(
        productGroupCode: Int
    ): List<Product>

    @Query(
        """
        select p from Product p
        where (:search is null
               or lower(coalesce(p.productName, '')) like lower(concat('%', :search, '%'))
               or str(p.productCode) like concat('%', :search, '%'))
          and (:productGroupCode is null or p.productGroupCode = :productGroupCode)
          and (:isDiscounts is null or p.isDiscounts = :isDiscounts)
          and (:isTax is null or p.isTax = :isTax)
        """
    )
    fun searchAdminProducts(
        @Param("search") search: String?,
        @Param("productGroupCode") productGroupCode: Int?,
        @Param("isDiscounts") isDiscounts: Boolean?,
        @Param("isTax") isTax: Boolean?,
        pageable: Pageable
    ): Page<Product>

    @Query(
        """
        select p from Product p
        where (:search is null
               or lower(coalesce(p.productName, '')) like lower(concat('%', :search, '%'))
               or str(p.productCode) like concat('%', :search, '%'))
          and (:categoryCode is null or p.productGroupCode = :categoryCode)
          and (:hasDiscount is null or p.isDiscounts = :hasDiscount)
        """
    )
    fun searchPublicProducts(
        @Param("search") search: String?,
        @Param("categoryCode") categoryCode: Int?,
        @Param("hasDiscount") hasDiscount: Boolean?,
        pageable: Pageable
    ): Page<Product>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
    select p
    from Product p
    where p.productCode in :productCodes
    """
    )
    fun findByProductCodeInForUpdate(
        @Param("productCodes") productCodes: Collection<Int>
    ): List<Product>
}