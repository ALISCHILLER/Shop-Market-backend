package com.msa.eshop.backend.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "customers")
open class Customer(
    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    open var id: UUID? = null,

    @Column(name = "customer_code", nullable = false, unique = true)
    open var customerCode: String = "",

    @Column(name = "customer_name", nullable = false)
    open var customerName: String = "",

    @Column(name = "mobile")
    open var mobile: String? = null,

    @Column(name = "phone")
    open var phone: String? = null,

    @Column(name = "center")
    open var center: String? = null,

    @Column(name = "national_code")
    open var nationalCode: String? = null,

    @Column(name = "password_hash", nullable = false)
    open var passwordHash: String = "",

    @Column(name = "salt")
    open var salt: String? = null,

    @Column(name = "role", nullable = false)
    open var role: String = "CUSTOMER",

    @Column(name = "enabled", nullable = false)
    open var enabled: Boolean = true,

    @Column(name = "created_at", nullable = false)
    open var createdAt: OffsetDateTime = OffsetDateTime.now()
)

@Entity
@Table(name = "product_categories")
open class ProductCategory(
    @Id
    @Column(name = "product_category_code")
    open var productCategoryCode: Int = 0,

    @Column(name = "product_category_name")
    open var productCategoryName: String? = null,

    @Column(name = "product_category_image")
    open var productCategoryImage: String? = null,

    @Column(name = "product_category_image_unselect")
    open var productCategoryImageUnselect: String? = null
)

@Entity
@Table(name = "products")
open class Product(
    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    open var id: UUID? = null,

    @Column(name = "product_name")
    open var productName: String? = null,

    @Column(name = "product_code", nullable = false, unique = true)
    open var productCode: Int = 0,

    @Column(name = "full_name_kala1")
    open var fullNameKala1: String? = null,

    @Column(name = "unit1")
    open var unit1: String? = null,

    @Column(name = "unitid1")
    open var unitid1: String? = null,

    @Column(name = "convert_factor1", nullable = false)
    open var convertFactor1: Int = 1,

    @Column(name = "full_name_kala2")
    open var fullNameKala2: String? = null,

    @Column(name = "unit2")
    open var unit2: String? = null,

    @Column(name = "convert_factor2", nullable = false)
    open var convertFactor2: Int = 1,

    @Column(name = "unitid2")
    open var unitid2: String? = null,

    @Column(name = "product_group_code", nullable = false)
    open var productGroupCode: Int = 0,

    @Column(name = "price", nullable = false)
    open var price: Int = 0,

    @Column(name = "is_discounts", nullable = false)
    open var isDiscounts: Boolean = false,

    @Column(name = "is_tax", nullable = false)
    open var isTax: Boolean = true,

    @Column(name = "product_image")
    open var productImage: String? = null
)

@Entity
@Table(name = "discounts")
open class Discount(
    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    open var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    open var product: Product? = null,

    @Column(name = "discount_percent", nullable = false)
    open var discountPercent: Int = 0,

    @Column(name = "from_number", nullable = false)
    open var fromNumber: Int = 1,

    @Column(name = "end_number", nullable = false)
    open var endNumber: Int = Int.MAX_VALUE
)

@Entity
@Table(name = "banners")
open class Banner(
    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    open var id: UUID? = null,

    @Column(name = "banner_image", nullable = false)
    open var bannerImage: String = "",

    @Column(name = "banner_name", nullable = false)
    open var bannerName: String = ""
)

@Entity
@Table(name = "customer_addresses")
open class CustomerAddress(
    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    open var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    open var customer: Customer? = null,

    @Column(name = "center_name", nullable = false)
    open var centerName: String = "",

    @Column(name = "customer_address", nullable = false)
    open var customerAddress: String = "",

    @Column(name = "customer_mobile", nullable = false)
    open var customerMobile: String = "",

    @Column(name = "customer_phone", nullable = false)
    open var customerPhone: String = ""
)

@Entity
@Table(name = "payment_terms")
open class PaymentTerm(
    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    open var id: UUID? = null,

    @Column(name = "name", nullable = false)
    open var name: String = "",

    @Column(name = "dead_line", nullable = false)
    open var deadLine: Int = 0,

    @Column(name = "immediate_discount_percent", nullable = false)
    open var immediateDiscountPercent: Int = 0,

    @Column(name = "receipt_discount_percent", nullable = false)
    open var receiptDiscountPercent: Int = 0,

    @Column(name = "cheque_discount_percent", nullable = false)
    open var chequeDiscountPercent: Int = 0,

    @Column(name = "active", nullable = false)
    open var active: Boolean = true
)

@Entity
@Table(name = "carts")
open class Cart(
    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    open var id: UUID? = null,

    @Column(name = "cart_code", nullable = false, unique = true)
    open var cartCode: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    open var customer: Customer? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_address_id", nullable = false)
    open var address: CustomerAddress? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_term_id", nullable = false)
    open var paymentTerm: PaymentTerm? = null,

    @Column(name = "customer_name_snapshot", nullable = false)
    open var customerNameSnapshot: String = "",

    @Column(name = "customer_address_snapshot", nullable = false)
    open var customerAddressSnapshot: String = "",

    @Column(name = "status_name", nullable = false)
    open var statusName: String = "ثبت شده",

    @Column(name = "status_color", nullable = false)
    open var statusColor: String = "#2E7D32",

    @Column(name = "sales_date", nullable = false)
    open var salesDate: LocalDate = LocalDate.now(),

    @Column(name = "subtotal", nullable = false)
    open var subtotal: Int = 0,

    @Column(name = "discount_total", nullable = false)
    open var discountTotal: Int = 0,

    @Column(name = "tax_total", nullable = false)
    open var taxTotal: Int = 0,

    @Column(name = "total", nullable = false)
    open var total: Int = 0,

    @Column(name = "created_at", nullable = false)
    open var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    open var items: MutableList<CartItem> = mutableListOf()
) {
    fun addItem(item: CartItem) {
        item.cart = this
        items.add(item)
    }
}

@Entity
@Table(name = "cart_items")
open class CartItem(
    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    open var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    open var cart: Cart? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    open var product: Product? = null,

    @Column(name = "product_code", nullable = false)
    open var productCode: Int = 0,

    @Column(name = "product_name", nullable = false)
    open var productName: String = "",

    @Column(name = "product_image_url")
    open var productImageUrl: String? = null,

    @Column(name = "quantity", nullable = false)
    open var quantity: Int = 0,

    @Column(name = "price", nullable = false)
    open var price: Int = 0,

    @Column(name = "discount", nullable = false)
    open var discount: Int = 0,

    @Column(name = "tax", nullable = false)
    open var tax: Int = 0,

    @Column(name = "total", nullable = false)
    open var total: Int = 0
)
