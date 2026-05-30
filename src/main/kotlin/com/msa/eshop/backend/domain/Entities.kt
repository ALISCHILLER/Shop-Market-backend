package com.msa.eshop.backend.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.Version
import org.hibernate.annotations.Check
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@MappedSuperclass
abstract class BaseUuidEntity {
    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    open var id: UUID? = null
}

@MappedSuperclass
abstract class AuditableUuidEntity : BaseUuidEntity() {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    open var createdAt: OffsetDateTime = OffsetDateTime.now()

    @UpdateTimestamp
    @Column(name = "updated_at")
    open var updatedAt: OffsetDateTime? = null

    @Version
    @Column(name = "version", nullable = false)
    open var version: Long = 0
}

@Entity
@Table(
    name = "customers",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_customers_customer_code",
            columnNames = ["customer_code"]
        )
    ],
    indexes = [
        Index(name = "idx_customers_role", columnList = "role"),
        Index(name = "idx_customers_enabled", columnList = "enabled"),
        Index(name = "idx_customers_created_at", columnList = "created_at")
    ]
)
@Check(constraints = "role in ('ADMIN', 'CUSTOMER')")
open class Customer(
    @Column(name = "customer_code", nullable = false, length = 64)
    open var customerCode: String = "",

    @Column(name = "customer_name", nullable = false, length = 255)
    open var customerName: String = "",

    @Column(name = "mobile", length = 32)
    open var mobile: String? = null,

    @Column(name = "phone", length = 32)
    open var phone: String? = null,

    @Column(name = "center", length = 255)
    open var center: String? = null,

    @Column(name = "national_code", length = 32)
    open var nationalCode: String? = null,

    @Column(name = "password_hash", nullable = false, length = 255)
    open var passwordHash: String = "",

    @Column(name = "salt", length = 255)
    open var salt: String? = null,

    @Column(name = "role", nullable = false, length = 32)
    open var role: String = CustomerRole.CUSTOMER.name,

    @Column(name = "enabled", nullable = false)
    open var enabled: Boolean = true
) : AuditableUuidEntity() {
    fun applyRole(role: CustomerRole) {
        this.role = role.name
    }

    fun isAdmin(): Boolean =
        CustomerRole.normalize(role) == CustomerRole.ADMIN
}

@Entity
@Table(
    name = "product_categories",
    indexes = [
        Index(name = "idx_product_categories_name", columnList = "product_category_name")
    ]
)
open class ProductCategory(
    @Id
    @Column(name = "product_category_code", nullable = false)
    open var productCategoryCode: Int = 0,

    @Column(name = "product_category_name", length = 255)
    open var productCategoryName: String? = null,

    @Column(name = "product_category_image", columnDefinition = "text")
    open var productCategoryImage: String? = null,

    @Column(name = "product_category_image_unselect", columnDefinition = "text")
    open var productCategoryImageUnselect: String? = null
)

@Entity
@Table(
    name = "products",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_products_product_code",
            columnNames = ["product_code"]
        )
    ],
    indexes = [
        Index(name = "idx_products_group_code", columnList = "product_group_code"),
        Index(name = "idx_products_product_name", columnList = "product_name"),
        Index(name = "idx_products_is_discounts", columnList = "is_discounts"),
        Index(name = "idx_products_is_tax", columnList = "is_tax")
    ]
)
@Check(constraints = "price >= 0 and convert_factor1 > 0 and convert_factor2 > 0")
open class Product(
    @Column(name = "product_name", length = 255)
    open var productName: String? = null,

    @Column(name = "product_code", nullable = false)
    open var productCode: Int = 0,

    @Column(name = "full_name_kala1", length = 255)
    open var fullNameKala1: String? = null,

    @Column(name = "unit1", length = 64)
    open var unit1: String? = null,

    @Column(name = "unitid1", length = 64)
    open var unitid1: String? = null,

    @Column(name = "convert_factor1", nullable = false)
    open var convertFactor1: Int = 1,

    @Column(name = "full_name_kala2", length = 255)
    open var fullNameKala2: String? = null,

    @Column(name = "unit2", length = 64)
    open var unit2: String? = null,

    @Column(name = "convert_factor2", nullable = false)
    open var convertFactor2: Int = 1,

    @Column(name = "unitid2", length = 64)
    open var unitid2: String? = null,

    @Column(name = "product_group_code", nullable = false)
    open var productGroupCode: Int = 0,

    @Column(name = "price", nullable = false)
    open var price: Int = 0,

    @Column(name = "is_discounts", nullable = false)
    open var isDiscounts: Boolean = false,

    @Column(name = "is_tax", nullable = false)
    open var isTax: Boolean = true,

    @Column(name = "product_image", columnDefinition = "text")
    open var productImage: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_group_code",
        referencedColumnName = "product_category_code",
        insertable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_products_product_category")
    )
    open var category: ProductCategory? = null
) : AuditableUuidEntity()

@Entity
@Table(
    name = "discounts",
    indexes = [
        Index(name = "idx_discounts_product_id", columnList = "product_id"),
        Index(name = "idx_discounts_product_range", columnList = "product_id, from_number, end_number")
    ]
)
@Check(constraints = "discount_percent >= 0 and discount_percent <= 100 and from_number >= 1 and end_number >= from_number")
open class Discount(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_discounts_product")
    )
    open var product: Product? = null,

    @Column(name = "discount_percent", nullable = false)
    open var discountPercent: Int = 0,

    @Column(name = "from_number", nullable = false)
    open var fromNumber: Int = 1,

    @Column(name = "end_number", nullable = false)
    open var endNumber: Int = Int.MAX_VALUE
) : AuditableUuidEntity()

@Entity
@Table(
    name = "banners",
    indexes = [
        Index(name = "idx_banners_name", columnList = "banner_name")
    ]
)
open class Banner(
    @Column(name = "banner_image", nullable = false, columnDefinition = "text")
    open var bannerImage: String = "",

    @Column(name = "banner_name", nullable = false, length = 255)
    open var bannerName: String = ""
) : AuditableUuidEntity()

@Entity
@Table(
    name = "payment_terms",
    indexes = [
        Index(name = "idx_payment_terms_active_deadline", columnList = "active, dead_line")
    ]
)
@Check(
    constraints = "dead_line >= 0 and immediate_discount_percent >= 0 and immediate_discount_percent <= 100 and receipt_discount_percent >= 0 and receipt_discount_percent <= 100 and cheque_discount_percent >= 0 and cheque_discount_percent <= 100"
)
open class PaymentTerm(
    @Column(name = "name", nullable = false, length = 255)
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
) : AuditableUuidEntity()

@Entity
@Table(
    name = "customer_addresses",
    indexes = [
        Index(name = "idx_customer_addresses_customer_id", columnList = "customer_id"),
        Index(name = "idx_customer_addresses_location", columnList = "latitude, longitude")
    ]
)
@Check(
    constraints = "(latitude is null and longitude is null) or (latitude between -90 and 90 and longitude between -180 and 180)"
)
open class CustomerAddress(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "customer_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_customer_addresses_customer")
    )
    open var customer: Customer? = null,

    @Column(name = "center_name", nullable = false, length = 255)
    open var centerName: String = "",

    @Column(name = "customer_address", nullable = false, columnDefinition = "text")
    open var customerAddress: String = "",

    @Column(name = "customer_mobile", nullable = false, length = 32)
    open var customerMobile: String = "",

    @Column(name = "customer_phone", nullable = false, length = 32)
    open var customerPhone: String = "",

    @Column(name = "latitude")
    open var latitude: Double? = null,

    @Column(name = "longitude")
    open var longitude: Double? = null,

    @Column(name = "is_default", nullable = false)
    open var isDefault: Boolean = false
) : AuditableUuidEntity()

@Entity
@Table(
    name = "carts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_carts_cart_code",
            columnNames = ["cart_code"]
        )
    ],
    indexes = [
        Index(name = "idx_carts_customer_date", columnList = "customer_id, sales_date"),
        Index(name = "idx_carts_status_code", columnList = "status_code"),
        Index(name = "idx_carts_status_date", columnList = "status_code, sales_date"),
        Index(name = "idx_carts_created_at", columnList = "created_at")
    ]
)
@Check(constraints = "subtotal >= 0 and discount_total >= 0 and tax_total >= 0 and total >= 0")
open class Cart(
    @Column(name = "cart_code", nullable = false)
    open var cartCode: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "customer_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_carts_customer")
    )
    open var customer: Customer? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "customer_address_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_carts_customer_address")
    )
    open var address: CustomerAddress? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "payment_term_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_carts_payment_term")
    )
    open var paymentTerm: PaymentTerm? = null,

    @Column(name = "customer_name_snapshot", nullable = false, length = 255)
    open var customerNameSnapshot: String = "",

    @Column(name = "customer_address_snapshot", nullable = false, columnDefinition = "text")
    open var customerAddressSnapshot: String = "",

    @Column(name = "status_code", nullable = false, length = 32)
    open var statusCode: String = CartStatus.REGISTERED.code,

    @Column(name = "status_name", nullable = false, length = 128)
    open var statusName: String = CartStatus.REGISTERED.title,

    @Column(name = "status_color", nullable = false, length = 32)
    open var statusColor: String = CartStatus.REGISTERED.color,

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

    @OneToMany(
        mappedBy = "cart",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    open var items: MutableList<CartItem> = mutableListOf()
) : AuditableUuidEntity() {
    fun addItem(item: CartItem) {
        item.cart = this
        items.add(item)
    }

    fun applyStatus(
        status: CartStatus,
        customColor: String? = null
    ) {
        statusCode = status.code
        statusName = status.title
        statusColor = customColor?.takeIf { it.isNotBlank() } ?: status.color
    }
}

@Entity
@Table(
    name = "cart_items",
    indexes = [
        Index(name = "idx_cart_items_cart_id", columnList = "cart_id"),
        Index(name = "idx_cart_items_product_id", columnList = "product_id"),
        Index(name = "idx_cart_items_product_code", columnList = "product_code")
    ]
)
@Check(constraints = "quantity > 0 and price >= 0 and discount >= 0 and tax >= 0 and total >= 0")
open class CartItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "cart_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_cart_items_cart")
    )
    open var cart: Cart? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_cart_items_product")
    )
    open var product: Product? = null,

    @Column(name = "product_code", nullable = false)
    open var productCode: Int = 0,

    @Column(name = "product_name", nullable = false, length = 255)
    open var productName: String = "",

    @Column(name = "product_image_url", columnDefinition = "text")
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
) : AuditableUuidEntity()