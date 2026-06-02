package com.msa.eshop.backend.domain.repository

import com.msa.eshop.backend.domain.entity.Discount
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface DiscountRepository : JpaRepository<Discount, UUID> {

    @EntityGraph(attributePaths = ["product"])
    fun findByProductId(productId: UUID): List<Discount>

    @EntityGraph(attributePaths = ["product"])
    fun findByProductIdIn(productIds: Collection<UUID>): List<Discount>

    @EntityGraph(attributePaths = ["product"])
    fun findByProductProductCode(productCode: Int): List<Discount>

    @EntityGraph(attributePaths = ["product"])
    fun findAllByOrderByFromNumberAsc(): List<Discount>

    @EntityGraph(attributePaths = ["product"])
    @Query(
        """
        select d
        from Discount d
        join d.product p
        where (
            :productId is null
            or p.id = :productId
        )
        and (
            :productCode is null
            or p.productCode = :productCode
        )
        and (
            :search is null
            or lower(coalesce(p.productName, '')) like lower(concat('%', :search, '%'))
            or str(p.productCode) like concat('%', :search, '%')
        )
        """
    )
    fun searchAdminDiscounts(
        @Param("productId") productId: UUID?,
        @Param("productCode") productCode: Int?,
        @Param("search") search: String?,
        pageable: Pageable
    ): Page<Discount>

    @Query(
        """
        select case when count(d) > 0 then true else false end
        from Discount d
        where d.product.id = :productId
          and (:exceptDiscountId is null or d.id <> :exceptDiscountId)
          and :fromNumber <= d.endNumber
          and :endNumber >= d.fromNumber
        """
    )
    fun existsOverlappingRange(
        @Param("productId") productId: UUID,
        @Param("fromNumber") fromNumber: Int,
        @Param("endNumber") endNumber: Int,
        @Param("exceptDiscountId") exceptDiscountId: UUID?
    ): Boolean
}