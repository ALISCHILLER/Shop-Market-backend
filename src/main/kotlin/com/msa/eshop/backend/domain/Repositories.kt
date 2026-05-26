package com.msa.eshop.backend.domain

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.UUID

interface CustomerRepository : JpaRepository<Customer, UUID> {
    fun findByCustomerCode(customerCode: String): Customer?
    fun existsByCustomerCode(customerCode: String): Boolean
}

interface ProductCategoryRepository : JpaRepository<ProductCategory, Int> {
    fun findAllByOrderByProductCategoryCodeAsc(): List<ProductCategory>
}

interface ProductRepository : JpaRepository<Product, UUID> {
    fun findByProductCode(productCode: Int): Product?
    fun findAllByOrderByProductNameAsc(): List<Product>
    fun findByProductGroupCodeOrderByProductNameAsc(productGroupCode: Int): List<Product>
}

interface DiscountRepository : JpaRepository<Discount, UUID> {
    @EntityGraph(attributePaths = ["product"])
    fun findByProductId(productId: UUID): List<Discount>

    @EntityGraph(attributePaths = ["product"])
    fun findByProductProductCode(productCode: Int): List<Discount>

    @EntityGraph(attributePaths = ["product"])
    fun findAllByOrderByFromNumberAsc(): List<Discount>
}

interface BannerRepository : JpaRepository<Banner, UUID> {
    fun findAllByOrderByBannerNameAsc(): List<Banner>
}

interface CustomerAddressRepository : JpaRepository<CustomerAddress, UUID> {
    @EntityGraph(attributePaths = ["customer"])
    fun findByCustomerId(customerId: UUID): List<CustomerAddress>
}

interface PaymentTermRepository : JpaRepository<PaymentTerm, UUID> {
    fun findByActiveTrueOrderByDeadLineAsc(): List<PaymentTerm>
    fun findFirstByActiveTrueOrderByDeadLineAsc(): PaymentTerm?
}

interface CartRepository : JpaRepository<Cart, UUID> {
    @EntityGraph(attributePaths = ["customer", "address", "paymentTerm", "items", "items.product"])
    fun findByCartCode(cartCode: Int): Cart?

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

    @Query("select coalesce(sum(c.total), 0) from Cart c")
    fun revenue(): Long
}
