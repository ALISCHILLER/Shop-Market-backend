package com.msa.eshop.backend.domain.repository

import com.msa.eshop.backend.domain.entity.CartItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface CartItemRepository : JpaRepository<CartItem, UUID> {

    fun countByProductId(productId: UUID): Long

    @Query(
        """
        select ci.cart.id as cartId, count(ci.id) as itemCount
        from CartItem ci
        where ci.cart.id in :cartIds
        group by ci.cart.id
        """
    )
    fun countItemsByCartIds(
        @Param("cartIds") cartIds: Collection<UUID>
    ): List<CartItemCountProjection>
}

interface CartItemCountProjection {
    val cartId: UUID
    val itemCount: Long
}