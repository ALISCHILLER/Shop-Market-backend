package com.msa.eshop.backend.domain

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
interface CustomerRepository : JpaRepository<Customer, UUID> {
    fun findByCustomerCode(customerCode: String): Customer?
    fun existsByCustomerCode(customerCode: String): Boolean
    fun existsByCustomerCodeAndIdNot(customerCode: String, id: UUID): Boolean

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

interface ProductCategoryRepository : JpaRepository<ProductCategory, Int> {
    fun findAllByOrderByProductCategoryCodeAsc(): List<ProductCategory>
}

interface ProductRepository : JpaRepository<Product, UUID> {
    fun findByProductCode(productCode: Int): Product?
    fun findByProductCodeIn(productCodes: Collection<Int>): List<Product>

    fun existsByProductCode(productCode: Int): Boolean
    fun existsByProductCodeAndIdNot(productCode: Int, id: UUID): Boolean

    fun countByProductGroupCode(productGroupCode: Int): Long

    fun findAllByOrderByProductNameAsc(): List<Product>
    fun findByProductGroupCodeOrderByProductNameAsc(productGroupCode: Int): List<Product>

    @Query(
        """
        select p from Product p
        where (:search is null
               or lower(coalesce(p.productName, '')) like lower(concat('%', :search, '%'))
               or cast(p.productCode as string) like concat('%', :search, '%'))
          and (:productGroupCode is null or p.productGroupCode = :productGroupCode)
          and (:isDiscounts is null or p.isDiscounts = :isDiscounts)
          and (:isTax is null or p.isTax = :isTax)
        """
    )
    fun searchAdminProducts(
        @Param("search") search: String?,
        @Param("productGroupCode") productGroupCode: Int?,
        @Param("isDiscounts") isDiscounts: Boolean?,
        @Param("isTax") isTax: Boolean?,
        pageable: Pageable
    ): Page<Product>
}

interface DiscountRepository : JpaRepository<Discount, UUID> {
    @EntityGraph(attributePaths = ["product"])
    fun findByProductId(productId: UUID): List<Discount>

    @EntityGraph(attributePaths = ["product"])
    fun findByProductIdIn(productIds: Collection<UUID>): List<Discount>

    @EntityGraph(attributePaths = ["product"])
    fun findByProductProductCode(productCode: Int): List<Discount>

    @EntityGraph(attributePaths = ["product"])
    fun findAllByOrderByFromNumberAsc(): List<Discount>

    @Query(
        """
        select case when count(d) > 0 then true else false end
        from Discount d
        where d.product.id = :productId
          and (:exceptDiscountId is null or d.id <> :exceptDiscountId)
          and :fromNumber <= d.endNumber
          and :endNumber >= d.fromNumber
        """
    )
    fun existsOverlappingRange(
        @Param("productId") productId: UUID,
        @Param("fromNumber") fromNumber: Int,
        @Param("endNumber") endNumber: Int,
        @Param("exceptDiscountId") exceptDiscountId: UUID?
    ): Boolean
}

interface BannerRepository : JpaRepository<Banner, UUID> {
    fun findAllByOrderByBannerNameAsc(): List<Banner>
}

interface CustomerAddressRepository : JpaRepository<CustomerAddress, UUID> {
    @EntityGraph(attributePaths = ["customer"])
    fun findByCustomerId(customerId: UUID): List<CustomerAddress>

    fun countByCustomerId(customerId: UUID): Long

    fun findFirstByCustomerIdOrderByCenterNameAsc(customerId: UUID): CustomerAddress?

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

interface PaymentTermRepository : JpaRepository<PaymentTerm, UUID> {
    fun findAllByOrderByDeadLineAscNameAsc(): List<PaymentTerm>
    fun findByActiveTrueOrderByDeadLineAsc(): List<PaymentTerm>
    fun findFirstByActiveTrueOrderByDeadLineAsc(): PaymentTerm?
}

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
          and (:fromDate is null or c.salesDate >= :fromDate)
          and (:toDate is null or c.salesDate <= :toDate)
        """
    )
    fun findAdminCarts(
        @Param("cartCode") cartCode: Int?,
        @Param("customerSearch") customerSearch: String?,
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