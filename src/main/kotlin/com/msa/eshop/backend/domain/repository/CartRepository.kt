package com.msa.eshop.backend.domain.repository

import com.msa.eshop.backend.domain.entity.Cart
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.UUID

interface CartRepository : JpaRepository<Cart, UUID> {

    @EntityGraph(attributePaths = ["customer", "address", "paymentTerm", "items", "items.product"])
    fun findByCartCode(cartCode: Int): Cart?

    fun countByCustomerId(customerId: UUID): Long

    fun countByAddressId(addressId: UUID): Long

    fun countByPaymentTermId(paymentTermId: UUID): Long

    @EntityGraph(attributePaths = ["customer", "address"])
    @Query(
        """
        select c from Cart c
        where c.customer.id = :customerId
          and (:fromDate is null or c.salesDate >= :fromDate)
          and (:toDate is null or c.salesDate <= :toDate)
        order by c.salesDate desc, c.cartCode desc
        """
    )
    fun findHistory(
        @Param("customerId") customerId: UUID,
        @Param("fromDate") fromDate: LocalDate?,
        @Param("toDate") toDate: LocalDate?
    ): List<Cart>

    @EntityGraph(attributePaths = ["customer", "address", "paymentTerm"])
    @Query(
        """
        select c from Cart c
        where (:cartCode is null or c.cartCode = :cartCode)
          and (:customerSearch is null
               or lower(c.customerNameSnapshot) like lower(concat('%', :customerSearch, '%'))
               or lower(c.customer.customerCode) like lower(concat('%', :customerSearch, '%')))
          and (:statusCode is null or c.statusCode = :statusCode)
          and (:fromDate is null or c.salesDate >= :fromDate)
          and (:toDate is null or c.salesDate <= :toDate)
        """
    )
    fun findAdminCarts(
        @Param("cartCode") cartCode: Int?,
        @Param("customerSearch") customerSearch: String?,
        @Param("statusCode") statusCode: String?,
        @Param("fromDate") fromDate: LocalDate?,
        @Param("toDate") toDate: LocalDate?,
        pageable: Pageable
    ): Page<Cart>

    @Query("select coalesce(sum(c.total), 0) from Cart c")
    fun revenue(): Long

    @Query(
        """
        select coalesce(sum(c.total), 0)
        from Cart c
        where (:fromDate is null or c.salesDate >= :fromDate)
          and (:toDate is null or c.salesDate <= :toDate)
        """
    )
    fun revenueBetween(
        @Param("fromDate") fromDate: LocalDate?,
        @Param("toDate") toDate: LocalDate?
    ): Long
}