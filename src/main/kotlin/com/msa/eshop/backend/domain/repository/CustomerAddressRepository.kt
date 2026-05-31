package com.msa.eshop.backend.domain.repository

import com.msa.eshop.backend.domain.entity.CustomerAddress
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface CustomerAddressRepository : JpaRepository<CustomerAddress, UUID> {

    @EntityGraph(attributePaths = ["customer"])
    fun findByCustomerId(customerId: UUID): List<CustomerAddress>

    fun countByCustomerId(customerId: UUID): Long

    fun findFirstByCustomerIdOrderByCenterNameAsc(
        customerId: UUID
    ): CustomerAddress?

    @Modifying(clearAutomatically = false, flushAutomatically = true)
    @Query(
        """
        update CustomerAddress a
        set a.isDefault = false
        where a.customer.id = :customerId
          and (:exceptId is null or a.id <> :exceptId)
        """
    )
    fun clearDefaultForCustomer(
        @Param("customerId") customerId: UUID,
        @Param("exceptId") exceptId: UUID?
    ): Int
}