package com.msa.eshop.backend.service

import com.msa.eshop.backend.common.BannerDto
import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.DashboardDto
import com.msa.eshop.backend.common.DiscountResultDto
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.OrderAddressDto
import com.msa.eshop.backend.common.PaymentTermDto
import com.msa.eshop.backend.common.ProductDto
import com.msa.eshop.backend.common.ProductGroupDto
import com.msa.eshop.backend.common.UpsertAddressRequest
import com.msa.eshop.backend.common.UpsertBannerRequest
import com.msa.eshop.backend.common.UpsertCustomerRequest
import com.msa.eshop.backend.common.UpsertDiscountRequest
import com.msa.eshop.backend.common.UpsertPaymentTermRequest
import com.msa.eshop.backend.common.UpsertProductGroupRequest
import com.msa.eshop.backend.common.UpsertProductRequest
import com.msa.eshop.backend.common.UserDto
import com.msa.eshop.backend.domain.Banner
import com.msa.eshop.backend.domain.BannerRepository
import com.msa.eshop.backend.domain.CartRepository
import com.msa.eshop.backend.domain.Customer
import com.msa.eshop.backend.domain.CustomerAddress
import com.msa.eshop.backend.domain.CustomerAddressRepository
import com.msa.eshop.backend.domain.CustomerRepository
import com.msa.eshop.backend.domain.Discount
import com.msa.eshop.backend.domain.DiscountRepository
import com.msa.eshop.backend.domain.PaymentTerm
import com.msa.eshop.backend.domain.PaymentTermRepository
import com.msa.eshop.backend.domain.Product
import com.msa.eshop.backend.domain.ProductCategory
import com.msa.eshop.backend.domain.ProductCategoryRepository
import com.msa.eshop.backend.domain.ProductRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminService(
    private val customerRepository: CustomerRepository,
    private val addressRepository: CustomerAddressRepository,
    private val categoryRepository: ProductCategoryRepository,
    private val productRepository: ProductRepository,
    private val discountRepository: DiscountRepository,
    private val bannerRepository: BannerRepository,
    private val paymentTermRepository: PaymentTermRepository,
    private val cartRepository: CartRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional(readOnly = true)
    fun dashboard(): DashboardDto = DashboardDto(
        customers = customerRepository.count(),
        products = productRepository.count(),
        categories = categoryRepository.count(),
        carts = cartRepository.count(),
        revenue = cartRepository.revenue()
    )

    @Transactional(readOnly = true)
    fun customers(): List<UserDto> = customerRepository.findAll().map { it.toDto() }

    @Transactional
    fun createCustomer(request: UpsertCustomerRequest): UserDto {
        if (customerRepository.existsByCustomerCode(request.customerCode.trim())) {
            throw BadRequestException("کد مشتری قبلاً ثبت شده است")
        }
        val rawPassword = request.password?.takeIf { it.isNotBlank() } ?: "123456"
        val customer = Customer(
            customerCode = request.customerCode.trim(),
            customerName = request.customerName.trim(),
            mobile = request.mobile,
            phone = request.phone,
            center = request.center,
            nationalCode = request.nationalCode,
            passwordHash = passwordEncoder.encode(rawPassword),
            salt = "bcrypt",
            role = request.role.uppercase(),
            enabled = request.enabled
        )
        return customerRepository.save(customer).toDto()
    }

    @Transactional
    fun updateCustomer(id: UUID, request: UpsertCustomerRequest): UserDto {
        val customer = customerRepository.findById(id).orElseThrow { NotFoundException("مشتری پیدا نشد") }
        customer.customerCode = request.customerCode.trim()
        customer.customerName = request.customerName.trim()
        customer.mobile = request.mobile
        customer.phone = request.phone
        customer.center = request.center
        customer.nationalCode = request.nationalCode
        customer.role = request.role.uppercase()
        customer.enabled = request.enabled
        if (!request.password.isNullOrBlank()) {
            customer.passwordHash = passwordEncoder.encode(request.password)
            customer.salt = "bcrypt"
        }
        return customerRepository.save(customer).toDto()
    }

    @Transactional
    fun deleteCustomer(id: UUID) = deleteOrNotFound(customerRepository, id, "مشتری پیدا نشد")

    @Transactional(readOnly = true)
    fun addresses(customerId: UUID? = null): List<OrderAddressDto> {
        return if (customerId == null) {
            addressRepository.findAll().map { it.toDto() }
        } else {
            addressRepository.findByCustomerId(customerId).map { it.toDto() }
        }
    }

    @Transactional
    fun createAddress(request: UpsertAddressRequest): OrderAddressDto {
        val customer = customerRepository.findById(request.customerId.toUuid("شناسه مشتری معتبر نیست"))
            .orElseThrow { NotFoundException("مشتری پیدا نشد") }
        val address = CustomerAddress(
            customer = customer,
            centerName = request.centerName,
            customerAddress = request.customerAddress,
            customerMobile = request.customerMobile,
            customerPhone = request.customerPhone
        )
        return addressRepository.save(address).toDto()
    }

    @Transactional
    fun updateAddress(id: UUID, request: UpsertAddressRequest): OrderAddressDto {
        val address = addressRepository.findById(id).orElseThrow { NotFoundException("آدرس پیدا نشد") }
        val customer = customerRepository.findById(request.customerId.toUuid("شناسه مشتری معتبر نیست"))
            .orElseThrow { NotFoundException("مشتری پیدا نشد") }
        address.customer = customer
        address.centerName = request.centerName
        address.customerAddress = request.customerAddress
        address.customerMobile = request.customerMobile
        address.customerPhone = request.customerPhone
        return addressRepository.save(address).toDto()
    }

    @Transactional
    fun deleteAddress(id: UUID) = deleteOrNotFound(addressRepository, id, "آدرس پیدا نشد")

    @Transactional(readOnly = true)
    fun productGroups(): List<ProductGroupDto> = categoryRepository.findAllByOrderByProductCategoryCodeAsc().map { it.toDto() }

    @Transactional
    fun upsertProductGroup(request: UpsertProductGroupRequest): ProductGroupDto {
        val category = categoryRepository.findById(request.productCategoryCode).orElse(ProductCategory(productCategoryCode = request.productCategoryCode))
        category.productCategoryName = request.productCategoryName
        category.productCategoryImage = request.productCategoryImage
        category.productCategoryImageUnselect = request.productCategoryImageUnselect
        return categoryRepository.save(category).toDto()
    }

    @Transactional
    fun deleteProductGroup(code: Int) = deleteOrNotFound(categoryRepository, code, "دسته‌بندی پیدا نشد")

    @Transactional(readOnly = true)
    fun products(): List<ProductDto> = productRepository.findAllByOrderByProductNameAsc().map { it.toDto() }

    @Transactional
    fun createProduct(request: UpsertProductRequest): ProductDto {
        requireCategory(request.productGroupCode)
        val product = Product()
        product.applyRequest(request)
        return productRepository.save(product).toDto()
    }

    @Transactional
    fun updateProduct(id: UUID, request: UpsertProductRequest): ProductDto {
        requireCategory(request.productGroupCode)
        val product = productRepository.findById(id).orElseThrow { NotFoundException("کالا پیدا نشد") }
        product.applyRequest(request)
        return productRepository.save(product).toDto()
    }

    @Transactional
    fun deleteProduct(id: UUID) = deleteOrNotFound(productRepository, id, "کالا پیدا نشد")

    @Transactional(readOnly = true)
    fun discounts(): List<DiscountResultDto> = discountRepository.findAllByOrderByFromNumberAsc().map { it.toDto() }

    @Transactional
    fun createDiscount(request: UpsertDiscountRequest): DiscountResultDto {
        val product = request.productId.findProduct()
        val discount = Discount(
            product = product,
            discountPercent = request.discountPercent,
            fromNumber = request.fromNumber,
            endNumber = request.endNumber
        )
        return discountRepository.save(discount).toDto()
    }

    @Transactional
    fun updateDiscount(id: UUID, request: UpsertDiscountRequest): DiscountResultDto {
        val discount = discountRepository.findById(id).orElseThrow { NotFoundException("تخفیف پیدا نشد") }
        discount.product = request.productId.findProduct()
        discount.discountPercent = request.discountPercent
        discount.fromNumber = request.fromNumber
        discount.endNumber = request.endNumber
        return discountRepository.save(discount).toDto()
    }

    @Transactional
    fun deleteDiscount(id: UUID) = deleteOrNotFound(discountRepository, id, "تخفیف پیدا نشد")

    @Transactional(readOnly = true)
    fun banners(): List<BannerDto> = bannerRepository.findAllByOrderByBannerNameAsc().map { it.toDto() }

    @Transactional
    fun createBanner(request: UpsertBannerRequest): BannerDto = bannerRepository.save(
        Banner(bannerImage = request.bannerImage, bannerName = request.bannerName)
    ).toDto()

    @Transactional
    fun updateBanner(id: UUID, request: UpsertBannerRequest): BannerDto {
        val banner = bannerRepository.findById(id).orElseThrow { NotFoundException("بنر پیدا نشد") }
        banner.bannerImage = request.bannerImage
        banner.bannerName = request.bannerName
        return bannerRepository.save(banner).toDto()
    }

    @Transactional
    fun deleteBanner(id: UUID) = deleteOrNotFound(bannerRepository, id, "بنر پیدا نشد")

    @Transactional(readOnly = true)
    fun paymentTerms(): List<PaymentTermDto> = paymentTermRepository.findByActiveTrueOrderByDeadLineAsc().map { it.toDto() }

    @Transactional
    fun createPaymentTerm(request: UpsertPaymentTermRequest): PaymentTermDto = paymentTermRepository.save(
        PaymentTerm(
            name = request.name,
            deadLine = request.deadLine,
            immediateDiscountPercent = request.immediateDiscountPercent,
            receiptDiscountPercent = request.receiptDiscountPercent,
            chequeDiscountPercent = request.chequeDiscountPercent,
            active = request.active
        )
    ).toDto()

    @Transactional
    fun updatePaymentTerm(id: UUID, request: UpsertPaymentTermRequest): PaymentTermDto {
        val term = paymentTermRepository.findById(id).orElseThrow { NotFoundException("روش پرداخت پیدا نشد") }
        term.name = request.name
        term.deadLine = request.deadLine
        term.immediateDiscountPercent = request.immediateDiscountPercent
        term.receiptDiscountPercent = request.receiptDiscountPercent
        term.chequeDiscountPercent = request.chequeDiscountPercent
        term.active = request.active
        return paymentTermRepository.save(term).toDto()
    }

    @Transactional
    fun deletePaymentTerm(id: UUID) = deleteOrNotFound(paymentTermRepository, id, "روش پرداخت پیدا نشد")

    private fun Product.applyRequest(request: UpsertProductRequest) {
        productName = request.productName
        productCode = request.productCode
        fullNameKala1 = request.fullNameKala1
        unit1 = request.unit1
        unitid1 = request.unitid1
        convertFactor1 = request.convertFactor1
        fullNameKala2 = request.fullNameKala2
        unit2 = request.unit2
        convertFactor2 = request.convertFactor2
        unitid2 = request.unitid2
        productGroupCode = request.productGroupCode
        price = request.price
        isDiscounts = request.isDiscounts
        productImage = request.productImage
    }

    private fun requireCategory(code: Int) {
        if (!categoryRepository.existsById(code)) throw BadRequestException("دسته‌بندی کالا پیدا نشد")
    }

    private fun String.findProduct(): Product {
        val asUuid = runCatching { UUID.fromString(this) }.getOrNull()
        if (asUuid != null) {
            return productRepository.findById(asUuid).orElseThrow { NotFoundException("کالا پیدا نشد") }
        }
        val asCode = toIntOrNull() ?: throw BadRequestException("شناسه کالا معتبر نیست")
        return productRepository.findByProductCode(asCode) ?: throw NotFoundException("کالا پیدا نشد")
    }

    private fun String.toUuid(message: String): UUID = runCatching { UUID.fromString(this) }
        .getOrElse { throw BadRequestException(message) }

    private fun <T : Any, ID : Any> deleteOrNotFound(repository: org.springframework.data.jpa.repository.JpaRepository<T, ID>, id: ID, message: String) {
        if (!repository.existsById(id)) throw NotFoundException(message)
        repository.deleteById(id)
    }
}
