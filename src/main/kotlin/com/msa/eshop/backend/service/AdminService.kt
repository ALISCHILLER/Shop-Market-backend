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
import com.msa.eshop.backend.domain.CartItemRepository
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
import org.springframework.data.jpa.repository.JpaRepository
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
    private val cartItemRepository: CartItemRepository,
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
    fun customers(): List<UserDto> =
        customerRepository.findAll().sortedBy { it.customerCode }.map { it.toDto() }

    @Transactional
    fun createCustomer(request: UpsertCustomerRequest): UserDto {
        val customerCode = request.customerCode.trim()
        if (customerRepository.existsByCustomerCode(customerCode)) {
            throw BadRequestException("کد مشتری قبلاً ثبت شده است")
        }

        val rawPassword = request.password?.trim()?.takeIf { it.isNotBlank() } ?: "123456"

        val customer = Customer(
            customerCode = customerCode,
            customerName = request.customerName.trim(),
            mobile = request.mobile.clean(),
            phone = request.phone.clean(),
            center = request.center.clean(),
            nationalCode = request.nationalCode.clean(),
            passwordHash = passwordEncoder.encode(rawPassword),
            salt = "bcrypt",
            role = normalizeRole(request.role),
            enabled = request.enabled
        )

        return customerRepository.save(customer).toDto()
    }

    @Transactional
    fun updateCustomer(id: UUID, request: UpsertCustomerRequest): UserDto {
        val customer = customerRepository.findById(id)
            .orElseThrow { NotFoundException("مشتری پیدا نشد") }

        val customerCode = request.customerCode.trim()
        if (customerRepository.existsByCustomerCodeAndIdNot(customerCode, id)) {
            throw BadRequestException("کد مشتری قبلاً برای مشتری دیگری ثبت شده است")
        }

        customer.customerCode = customerCode
        customer.customerName = request.customerName.trim()
        customer.mobile = request.mobile.clean()
        customer.phone = request.phone.clean()
        customer.center = request.center.clean()
        customer.nationalCode = request.nationalCode.clean()
        customer.role = normalizeRole(request.role)
        customer.enabled = request.enabled

        if (!request.password.isNullOrBlank()) {
            if (request.password.trim().length < 6) {
                throw BadRequestException("رمز عبور باید حداقل ۶ کاراکتر باشد")
            }
            customer.passwordHash = passwordEncoder.encode(request.password.trim())
            customer.salt = "bcrypt"
        }

        return customerRepository.save(customer).toDto()
    }

    @Transactional
    fun deleteCustomer(id: UUID) {
        if (!customerRepository.existsById(id)) throw NotFoundException("مشتری پیدا نشد")
        if (cartRepository.countByCustomerId(id) > 0) {
            throw BadRequestException("این مشتری دارای سفارش است و قابل حذف نیست")
        }
        customerRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun addresses(customerId: UUID? = null): List<OrderAddressDto> {
        val addresses = if (customerId == null) {
            addressRepository.findAll()
        } else {
            addressRepository.findByCustomerId(customerId)
        }

        return addresses
            .sortedWith(compareByDescending<CustomerAddress> { it.isDefault }.thenBy { it.centerName })
            .map { it.toDto() }
    }

    @Transactional
    fun createAddress(request: UpsertAddressRequest): OrderAddressDto {
        val customerId = request.customerId.toUuid("شناسه مشتری معتبر نیست")
        val customer = customerRepository.findById(customerId)
            .orElseThrow { NotFoundException("مشتری پیدا نشد") }

        val isFirstAddress = addressRepository.countByCustomerId(customerId) == 0L
        val shouldBeDefault = request.isDefault ?: isFirstAddress

        if (shouldBeDefault) clearDefaultAddress(customerId, exceptId = null)

        val address = CustomerAddress(
            customer = customer,
            centerName = request.centerName.trim(),
            customerAddress = request.customerAddress.trim(),
            customerMobile = request.customerMobile.trim(),
            customerPhone = request.customerPhone.trim(),
            latitude = request.resolvedLatitude(),
            longitude = request.resolvedLongitude(),
            isDefault = shouldBeDefault
        )

        validateGeo(address.latitude, address.longitude)

        return addressRepository.save(address).toDto()
    }

    @Transactional
    fun updateAddress(id: UUID, request: UpsertAddressRequest): OrderAddressDto {
        val address = addressRepository.findById(id)
            .orElseThrow { NotFoundException("آدرس پیدا نشد") }

        val customerId = request.customerId.toUuid("شناسه مشتری معتبر نیست")
        val customer = customerRepository.findById(customerId)
            .orElseThrow { NotFoundException("مشتری پیدا نشد") }

        val shouldBeDefault = request.isDefault ?: address.isDefault
        if (shouldBeDefault) clearDefaultAddress(customerId, exceptId = id)

        address.customer = customer
        address.centerName = request.centerName.trim()
        address.customerAddress = request.customerAddress.trim()
        address.customerMobile = request.customerMobile.trim()
        address.customerPhone = request.customerPhone.trim()
        address.latitude = request.resolvedLatitude()
        address.longitude = request.resolvedLongitude()
        address.isDefault = shouldBeDefault

        validateGeo(address.latitude, address.longitude)

        return addressRepository.save(address).toDto()
    }

    @Transactional
    fun deleteAddress(id: UUID) {
        if (!addressRepository.existsById(id)) throw NotFoundException("آدرس پیدا نشد")
        if (cartRepository.countByAddressId(id) > 0) {
            throw BadRequestException("این آدرس در سفارش استفاده شده و قابل حذف نیست")
        }
        addressRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun productGroups(): List<ProductGroupDto> =
        categoryRepository.findAllByOrderByProductCategoryCodeAsc().map { it.toDto() }

    @Transactional
    fun upsertProductGroup(request: UpsertProductGroupRequest): ProductGroupDto {
        val name = request.productCategoryName?.trim()
        if (name.isNullOrBlank()) throw BadRequestException("نام دسته‌بندی الزامی است")

        val category = categoryRepository.findById(request.productCategoryCode)
            .orElse(ProductCategory(productCategoryCode = request.productCategoryCode))

        category.productCategoryName = name
        category.productCategoryImage = request.productCategoryImage.clean()
        category.productCategoryImageUnselect = request.productCategoryImageUnselect.clean()

        return categoryRepository.save(category).toDto()
    }

    @Transactional
    fun deleteProductGroup(code: Int) {
        if (!categoryRepository.existsById(code)) throw NotFoundException("دسته‌بندی پیدا نشد")
        if (productRepository.countByProductGroupCode(code) > 0) {
            throw BadRequestException("این دسته‌بندی دارای کالا است و قابل حذف نیست")
        }
        categoryRepository.deleteById(code)
    }

    @Transactional(readOnly = true)
    fun products(): List<ProductDto> =
        productRepository.findAllByOrderByProductNameAsc().map { it.toDto() }

    @Transactional
    fun createProduct(request: UpsertProductRequest): ProductDto {
        validateProductRequest(request)
        requireCategory(request.productGroupCode)

        if (productRepository.existsByProductCode(request.productCode)) {
            throw BadRequestException("کد کالا قبلاً ثبت شده است")
        }

        val product = Product()
        product.applyRequest(request)

        return productRepository.save(product).toDto()
    }

    @Transactional
    fun updateProduct(id: UUID, request: UpsertProductRequest): ProductDto {
        validateProductRequest(request)
        requireCategory(request.productGroupCode)

        if (productRepository.existsByProductCodeAndIdNot(request.productCode, id)) {
            throw BadRequestException("کد کالا قبلاً برای کالای دیگری ثبت شده است")
        }

        val product = productRepository.findById(id)
            .orElseThrow { NotFoundException("کالا پیدا نشد") }

        product.applyRequest(request)

        return productRepository.save(product).toDto()
    }

    @Transactional
    fun deleteProduct(id: UUID) {
        if (!productRepository.existsById(id)) throw NotFoundException("کالا پیدا نشد")
        if (cartItemRepository.countByProductId(id) > 0) {
            throw BadRequestException("این کالا در سفارش استفاده شده و قابل حذف نیست")
        }
        productRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun discounts(): List<DiscountResultDto> =
        discountRepository.findAllByOrderByFromNumberAsc().map { it.toDto() }

    @Transactional
    fun createDiscount(request: UpsertDiscountRequest): DiscountResultDto {
        validateDiscountRequest(request)

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
        validateDiscountRequest(request)

        val discount = discountRepository.findById(id)
            .orElseThrow { NotFoundException("تخفیف پیدا نشد") }

        discount.product = request.productId.findProduct()
        discount.discountPercent = request.discountPercent
        discount.fromNumber = request.fromNumber
        discount.endNumber = request.endNumber

        return discountRepository.save(discount).toDto()
    }

    @Transactional
    fun deleteDiscount(id: UUID) =
        deleteOrNotFound(discountRepository, id, "تخفیف پیدا نشد")

    @Transactional(readOnly = true)
    fun banners(): List<BannerDto> =
        bannerRepository.findAllByOrderByBannerNameAsc().map { it.toDto() }

    @Transactional
    fun createBanner(request: UpsertBannerRequest): BannerDto {
        val banner = Banner(
            bannerImage = request.bannerImage.trim(),
            bannerName = request.bannerName.trim()
        )
        return bannerRepository.save(banner).toDto()
    }

    @Transactional
    fun updateBanner(id: UUID, request: UpsertBannerRequest): BannerDto {
        val banner = bannerRepository.findById(id)
            .orElseThrow { NotFoundException("بنر پیدا نشد") }

        banner.bannerImage = request.bannerImage.trim()
        banner.bannerName = request.bannerName.trim()

        return bannerRepository.save(banner).toDto()
    }

    @Transactional
    fun deleteBanner(id: UUID) =
        deleteOrNotFound(bannerRepository, id, "بنر پیدا نشد")

    @Transactional(readOnly = true)
    fun paymentTerms(): List<PaymentTermDto> =
        paymentTermRepository.findAllByOrderByDeadLineAscNameAsc().map { it.toDto() }

    @Transactional
    fun createPaymentTerm(request: UpsertPaymentTermRequest): PaymentTermDto {
        validatePaymentTermRequest(request)

        val term = PaymentTerm(
            name = request.name.trim(),
            deadLine = request.deadLine,
            immediateDiscountPercent = request.immediateDiscountPercent,
            receiptDiscountPercent = request.receiptDiscountPercent,
            chequeDiscountPercent = request.chequeDiscountPercent,
            active = request.active
        )

        return paymentTermRepository.save(term).toDto()
    }

    @Transactional
    fun updatePaymentTerm(id: UUID, request: UpsertPaymentTermRequest): PaymentTermDto {
        validatePaymentTermRequest(request)

        val term = paymentTermRepository.findById(id)
            .orElseThrow { NotFoundException("روش پرداخت پیدا نشد") }

        term.name = request.name.trim()
        term.deadLine = request.deadLine
        term.immediateDiscountPercent = request.immediateDiscountPercent
        term.receiptDiscountPercent = request.receiptDiscountPercent
        term.chequeDiscountPercent = request.chequeDiscountPercent
        term.active = request.active

        return paymentTermRepository.save(term).toDto()
    }

    @Transactional
    fun deletePaymentTerm(id: UUID) {
        if (!paymentTermRepository.existsById(id)) throw NotFoundException("روش پرداخت پیدا نشد")
        if (cartRepository.countByPaymentTermId(id) > 0) {
            throw BadRequestException("این روش پرداخت در سفارش استفاده شده و قابل حذف نیست")
        }
        paymentTermRepository.deleteById(id)
    }

    private fun Product.applyRequest(request: UpsertProductRequest) {
        productName = request.productName.clean()
        productCode = request.productCode
        fullNameKala1 = request.fullNameKala1.clean()
        unit1 = request.unit1.clean()
        unitid1 = request.unitid1.clean()
        convertFactor1 = request.convertFactor1
        fullNameKala2 = request.fullNameKala2.clean()
        unit2 = request.unit2.clean()
        convertFactor2 = request.convertFactor2
        unitid2 = request.unitid2.clean()
        productGroupCode = request.productGroupCode
        price = request.price
        isDiscounts = request.isDiscounts
        isTax = request.isTax
        productImage = request.productImage.clean()
    }

    private fun requireCategory(code: Int) {
        if (!categoryRepository.existsById(code)) {
            throw BadRequestException("دسته‌بندی کالا پیدا نشد")
        }
    }

    private fun validateProductRequest(request: UpsertProductRequest) {
        if (request.productCode <= 0) throw BadRequestException("کد کالا معتبر نیست")
        if (request.productGroupCode <= 0) throw BadRequestException("کد گروه کالا معتبر نیست")
        if (request.price < 0) throw BadRequestException("قیمت کالا معتبر نیست")
        if (request.convertFactor1 <= 0) throw BadRequestException("ضریب تبدیل واحد اول معتبر نیست")
        if (request.convertFactor2 <= 0) throw BadRequestException("ضریب تبدیل واحد دوم معتبر نیست")
    }

    private fun validateDiscountRequest(request: UpsertDiscountRequest) {
        if (request.discountPercent !in 0..100) throw BadRequestException("درصد تخفیف معتبر نیست")
        if (request.fromNumber < 1) throw BadRequestException("حداقل تعداد معتبر نیست")
        if (request.endNumber < request.fromNumber) {
            throw BadRequestException("حداکثر تعداد باید بزرگ‌تر یا مساوی حداقل تعداد باشد")
        }
    }

    private fun validatePaymentTermRequest(request: UpsertPaymentTermRequest) {
        if (request.name.isBlank()) throw BadRequestException("نام روش پرداخت الزامی است")
        if (request.deadLine < 0) throw BadRequestException("مهلت پرداخت معتبر نیست")
        listOf(
            request.immediateDiscountPercent,
            request.receiptDiscountPercent,
            request.chequeDiscountPercent
        ).forEach {
            if (it !in 0..100) throw BadRequestException("درصد تخفیف معتبر نیست")
        }
    }

    private fun validateGeo(latitude: Double?, longitude: Double?) {
        if ((latitude == null) != (longitude == null)) {
            throw BadRequestException("عرض و طول جغرافیایی باید با هم ارسال شوند")
        }
        if (latitude != null && latitude !in -90.0..90.0) {
            throw BadRequestException("عرض جغرافیایی معتبر نیست")
        }
        if (longitude != null && longitude !in -180.0..180.0) {
            throw BadRequestException("طول جغرافیایی معتبر نیست")
        }
    }

    private fun clearDefaultAddress(customerId: UUID, exceptId: UUID?) {
        addressRepository.findByCustomerId(customerId)
            .filter { it.id != exceptId && it.isDefault }
            .forEach { it.isDefault = false }
    }

    private fun normalizeRole(value: String): String {
        return when (value.trim().uppercase().removePrefix("ROLE_")) {
            "ADMIN" -> "ADMIN"
            "CUSTOMER" -> "CUSTOMER"
            else -> throw BadRequestException("نقش کاربر معتبر نیست")
        }
    }

    private fun String.findProduct(): Product {
        val value = trim()
        val asUuid = runCatching { UUID.fromString(value) }.getOrNull()

        if (asUuid != null) {
            return productRepository.findById(asUuid)
                .orElseThrow { NotFoundException("کالا پیدا نشد") }
        }

        val asCode = value.toIntOrNull()
            ?: throw BadRequestException("شناسه کالا معتبر نیست")

        return productRepository.findByProductCode(asCode)
            ?: throw NotFoundException("کالا پیدا نشد")
    }

    private fun String.toUuid(message: String): UUID =
        runCatching { UUID.fromString(this.trim()) }
            .getOrElse { throw BadRequestException(message) }

    private fun String?.clean(): String? =
        this?.trim()?.takeIf { it.isNotBlank() }

    private fun UpsertAddressRequest.resolvedLatitude(): Double? = latitude ?: lat

    private fun UpsertAddressRequest.resolvedLongitude(): Double? = longitude ?: lng

    private fun <T : Any, ID : Any> deleteOrNotFound(
        repository: JpaRepository<T, ID>,
        id: ID,
        message: String
    ) {
        if (!repository.existsById(id)) throw NotFoundException(message)
        repository.deleteById(id)
    }
}