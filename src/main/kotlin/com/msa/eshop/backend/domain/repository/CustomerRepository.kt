package com.msa.eshop.backend.domain.repository

import com.msa.eshop.backend.domain.entity.Customer
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface CustomerRepository : JpaRepository<Customer, UUID> {

    fun findByCustomerCode(customerCode: String): Customer?

    fun existsByCustomerCode(customerCode: String): Boolean

    fun existsByCustomerCodeAndIdNot(
        customerCode: String,
        id: UUID
    ): Boolean

    @Query(
        """
        select c from Customer c
        where (:search is null
               or lower(c.customerCode) like lower(concat('%', :search, '%'))
               or lower(c.customerName) like lower(concat('%', :search, '%'))
               or lower(coalesce(c.mobile, '')) like lower(concat('%', :search, '%')))
          and (:role is null or upper(c.role) = upper(:role))
          and (:enabled is null or c.enabled = :enabled)
        """
    )
    fun searchAdminCustomers(
        @Param("search") search: String?,
        @Param("role") role: String?,
        @Param("enabled") enabled: Boolean?,
        pageable: Pageable
    ): Page<Customer>
}