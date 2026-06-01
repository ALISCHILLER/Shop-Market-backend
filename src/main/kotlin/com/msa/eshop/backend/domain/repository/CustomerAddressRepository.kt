package com.msa.eshop.backend.domain.repository

import com.msa.eshop.backend.domain.entity.CustomerAddress
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    @EntityGraph(attributePaths = ["customer"])
    @Query(
        """
        select a
        from CustomerAddress a
        join a.customer c
        where (:customerId is null or c.id = :customerId)
          and (
              :search is null
              or lower(coalesce(a.centerName, '')) like lower(concat('%', :search, '%'))
              or lower(coalesce(a.customerAddress, '')) like lower(concat('%', :search, '%'))
              or lower(coalesce(a.customerMobile, '')) like lower(concat('%', :search, '%'))
              or lower(coalesce(a.customerPhone, '')) like lower(concat('%', :search, '%'))
              or lower(coalesce(c.customerCode, '')) like lower(concat('%', :search, '%'))
              or lower(coalesce(c.customerName, '')) like lower(concat('%', :search, '%'))
          )
        """
    )
    fun searchAdminAddresses(
        @Param("customerId") customerId: UUID?,
        @Param("search") search: String?,
        pageable: Pageable
    ): Page<CustomerAddress>

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
        @Param("customerId") customerId: UUID?,
        @Param("exceptId") exceptId: UUID?
    ): Int
}