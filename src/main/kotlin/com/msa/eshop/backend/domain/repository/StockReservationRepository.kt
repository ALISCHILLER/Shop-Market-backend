package com.msa.eshop.backend.domain.repository

import com.msa.eshop.backend.domain.entity.StockReservation
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface StockReservationRepository : JpaRepository<StockReservation, UUID> {

    fun findByCartIdAndStatus(
        cartId: UUID,
        status: String
    ): List<StockReservation>
}