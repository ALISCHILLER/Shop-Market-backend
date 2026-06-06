package com.msa.eshop.backend.domain.repository

import com.msa.eshop.backend.domain.entity.CartIdempotencyKey
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface CartIdempotencyKeyRepository : JpaRepository<CartIdempotencyKey, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = ["customer", "cart", "cart.items", "cart.paymentTerm"])
    @Query(
        """
        select k
        from CartIdempotencyKey k
        where k.customer.id = :customerId
          and k.endpoint = :endpoint
          and k.idempotencyKey = :idempotencyKey
        """
    )
    fun findForUpdate(
        @Param("customerId") customerId: UUID,
        @Param("endpoint") endpoint: String,
        @Param("idempotencyKey") idempotencyKey: String
    ): CartIdempotencyKey?
}