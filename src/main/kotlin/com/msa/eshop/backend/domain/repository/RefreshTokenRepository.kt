package com.msa.eshop.backend.domain.repository

import com.msa.eshop.backend.domain.entity.RefreshToken
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {

    fun findByTokenHash(tokenHash: String): RefreshToken?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select rt
        from RefreshToken rt
        join fetch rt.customer
        where rt.tokenHash = :tokenHash
        """
    )
    fun findByTokenHashForUpdate(
        @Param("tokenHash") tokenHash: String
    ): RefreshToken?

    fun findByCustomerIdAndRevokedAtIsNull(
        customerId: UUID
    ): List<RefreshToken>

    @Modifying
    @Query("delete from RefreshToken rt where rt.customer.id = :customerId")
    fun deleteByCustomerId(
        @Param("customerId") customerId: UUID
    ): Int
}