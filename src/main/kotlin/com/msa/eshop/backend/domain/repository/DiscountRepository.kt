package com.msa.eshop.backend.domain.repository

import com.msa.eshop.backend.domain.entity.Discount
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