package com.msa.eshop.backend.domain.repository

import com.msa.eshop.backend.domain.entity.PaymentTerm
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PaymentTermRepository : JpaRepository<PaymentTerm, UUID> {

    fun findAllByOrderByDeadLineAscNameAsc(): List<PaymentTerm>

    fun findByActiveTrueOrderByDeadLineAsc(): List<PaymentTerm>

    fun findFirstByActiveTrueOrderByDeadLineAsc(): PaymentTerm?

    fun findByIdAndActiveTrue(id: UUID): PaymentTerm?
}