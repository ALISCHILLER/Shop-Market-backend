package com.msa.eshop.backend.service

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.InsertCartModelRequest
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.OrderAddressDto
import com.msa.eshop.backend.common.PaymentTermDto
import com.msa.eshop.backend.common.ReportCartDetailsDto
import com.msa.eshop.backend.common.ReportHistoryCustomerDto
import com.msa.eshop.backend.common.ReportHistoryCustomerModelRequest
import com.msa.eshop.backend.common.SimulateDto
import com.msa.eshop.backend.common.SimulateModelRequest
import com.msa.eshop.backend.domain.Cart
import com.msa.eshop.backend.domain.CartItem
import com.msa.eshop.backend.domain.CartRepository
import com.msa.eshop.backend.domain.CustomerAddressRepository
import com.msa.eshop.backend.domain.CustomerRepository
import com.msa.eshop.backend.domain.DiscountRepository
import com.msa.eshop.backend.domain.PaymentTerm
import com.msa.eshop.backend.domain.PaymentTermRepository
import com.msa.eshop.backend.domain.Product
import com.msa.eshop.backend.domain.ProductRepository
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class CartService(
    private val currentUserService: CurrentUserService,
    private val customerRepository: CustomerRepository,
    private val addressRepository: CustomerAddressRepository,
    private val productRepository: ProductRepository,
    private val discountRepository: DiscountRepository,
    private val paymentTermRepository: PaymentTermRepository,
    private val cartRepository: CartRepository,
    private val entityManager: EntityManager,
    @Value("\${app.invoice.tax-percent:9}") private val taxPercent: Int
) {
    @Transactional(readOnly = true)
    fun simulate(requests: List<SimulateModelRequest>): List<SimulateDto> {
        if (requests.isEmpty()) return emptyList()

        val paymentTerm = paymentTermRepository.findFirstByActiveTrueOrderByDeadLineAsc()

        return requests
            .groupBy { it.productCode }
            .map { (productCode, rows) ->
                val quantity = rows.sumOf { it.quantity }
                if (quantity <= 0) throw BadRequestException("تعداد کالا باید بزرگ‌تر از صفر باشد")

                val product = productRepository.findByProductCode(productCode)
                    ?: throw NotFoundException("کالا با کد $productCode پیدا نشد")

                calculateSimulate(product, quantity, paymentTerm)
            }
    }

    @Transactional(readOnly = true)
    fun currentCustomerAddresses(): List<OrderAddressDto> {
        val customer = currentUserService.requireCustomer()

        return addressRepository.findByCustomerId(requireNotNull(customer.id))
            .sortedWith(compareByDescending<com.msa.eshop.backend.domain.CustomerAddress> { it.isDefault }.thenBy { it.centerName })
            .map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun paymentTerms(): List<PaymentTermDto> =
        paymentTermRepository.findByActiveTrueOrderByDeadLineAsc().map { it.toDto() }

    @Transactional
    fun insertCart(requests: List<InsertCartModelRequest>): Boolean {
        if (requests.isEmpty()) throw BadRequestException("سبد خرید خالی است")

        val customer = currentUserService.requireCustomer()
        val first = requests.first()

        validateSameHeader(requests, first)

        val addressId = first.customerAddressId.toUuidOrBadRequest("شناسه آدرس معتبر نیست")
        val paymentTermId = first.paymentTermId.toUuidOrBadRequest("شناسه روش پرداخت معتبر نیست")

        val address = addressRepository.findById(addressId)
            .orElseThrow { NotFoundException("آدرس سفارش پیدا نشد") }

        if (address.customer?.id != customer.id) {
            throw BadRequestException("آدرس انتخاب‌شده متعلق به این مشتری نیست")
        }

        val paymentTerm = paymentTermRepository.findById(paymentTermId)
            .orElseThrow { NotFoundException("روش پرداخت پیدا نشد") }

        if (!paymentTerm.active) {
            throw BadRequestException("روش پرداخت انتخاب‌شده غیرفعال است")
        }

        val normalizedLines = requests
            .groupBy { it.productCode }
            .map { (productCode, rows) ->
                NormalizedCartLine(
                    productCode = productCode,
                    quantity = rows.sumOf { it.quantity }
                )
            }

        normalizedLines.forEach {
            if (it.quantity <= 0) throw BadRequestException("تعداد کالا باید بزرگ‌تر از صفر باشد")
        }

        val cart = Cart(
            cartCode = nextCartCode(),
            customer = customer,
            address = address,
            paymentTerm = paymentTerm,
            customerNameSnapshot = customer.customerName,
            customerAddressSnapshot = address.customerAddress,
            statusName = "ثبت شده",
            statusColor = "#2E7D32",
            salesDate = LocalDate.now()
        )

        var subtotal = 0L
        var discountTotal = 0L
        var taxTotal = 0L
        var total = 0L

        normalizedLines.forEach { request ->
            val product = productRepository.findByProductCode(request.productCode)
                ?: throw NotFoundException("کالا با کد ${request.productCode} پیدا نشد")

            val line = calculateLine(product, request.quantity, paymentTerm)

            cart.addItem(
                CartItem(
                    product = product,
                    productCode = product.productCode,
                    productName = product.productName.orEmpty(),
                    productImageUrl = product.productImage,
                    quantity = request.quantity,
                    price = product.price,
                    discount = toPersistedAmount(line.discountAmount + line.paymentDiscountAmount),
                    tax = toPersistedAmount(line.taxAmount),
                    total = toPersistedAmount(line.total)
                )
            )

            subtotal += line.gross
            discountTotal += line.discountAmount + line.paymentDiscountAmount
            taxTotal += line.taxAmount
            total += line.total
        }

        cart.subtotal = toPersistedAmount(subtotal)
        cart.discountTotal = toPersistedAmount(discountTotal)
        cart.taxTotal = toPersistedAmount(taxTotal)
        cart.total = toPersistedAmount(total)

        cartRepository.save(cart)
        return true
    }

    @Transactional(readOnly = true)
    fun history(request: ReportHistoryCustomerModelRequest): List<ReportHistoryCustomerDto> {
        val current = currentUserService.requireCustomer()

        val customerId = request.customerId.trim()
            .takeIf { it.isNotBlank() }
            ?.toUuidOrBadRequest("شناسه مشتری معتبر نیست")
            ?: requireNotNull(current.id)

        if (customerId != current.id && current.role.uppercase() != "ADMIN") {
            throw BadRequestException("دسترسی به گزارش این مشتری مجاز نیست")
        }

        val fromDate = request.fromDate.toLocalDateOrNull()
        val toDate = request.endDate.toLocalDateOrNull()

        val customer = customerRepository.findById(customerId)
            .orElseThrow { NotFoundException("مشتری پیدا نشد") }

        return cartRepository.findHistory(requireNotNull(customer.id), fromDate, toDate)
            .map { it.toHistoryDto() }
    }

    @Transactional(readOnly = true)
    fun details(cartCode: Int): List<ReportCartDetailsDto> {
        val current = currentUserService.requireCustomer()

        val cart = cartRepository.findByCartCode(cartCode)
            ?: throw NotFoundException("سفارش پیدا نشد")

        if (cart.customer?.id != current.id && current.role.uppercase() != "ADMIN") {
            throw BadRequestException("دسترسی به جزئیات این سفارش مجاز نیست")
        }

        return cart.items.sortedBy { it.id.toString() }.map { it.toDetailsDto(cart) }
    }

    private fun calculateSimulate(
        product: Product,
        quantity: Int,
        paymentTerm: PaymentTerm?
    ): SimulateDto {
        val baseLine = calculateLine(product, quantity, paymentTerm)
        val immediate = calculateLine(product, quantity, paymentTerm, PaymentKind.IMMEDIATE)
        val cheque = calculateLine(product, quantity, paymentTerm, PaymentKind.CHEQUE)
        val receipt = calculateLine(product, quantity, paymentTerm, PaymentKind.RECEIPT)

        return SimulateDto(
            convertFactor1 = product.convertFactor1,
            convertFactor2 = product.convertFactor2,
            discountPercent = baseLine.productDiscountPercent,
            discount_Percent_PaymentTerm_Receipt = toClientAmount(receipt.paymentDiscountAmount),
            discount_Percent_PaymentTerm_Receipt_Tax = toClientAmount(receipt.taxAmount),
            discount_Percent_PaymentTerm_cheque = toClientAmount(cheque.paymentDiscountAmount),
            discount_Percent_PaymentTerm_cheque_Tax = toClientAmount(cheque.taxAmount),
            discount_Percent_PaymentTerm_immediate = toClientAmount(immediate.paymentDiscountAmount),
            discount_Percent_PaymentTerm_immediate_Tax = toClientAmount(immediate.taxAmount),
            finalPrice = toClientAmount(baseLine.gross),
            finalPriceDiscount = toClientAmount(baseLine.afterProductDiscount),
            fullNameKala1 = product.fullNameKala1.orEmpty(),
            fullNameKala2 = product.fullNameKala2.orEmpty(),
            id = requireNotNull(product.id).toString(),
            isTax = product.isTax,
            paymentTermId = paymentTerm?.id?.toString(),
            price = product.price,
            priceByDiscountPercent = toClientAmount(baseLine.afterProductDiscount),
            priceByDiscountPercentAndTax = toClientAmount(baseLine.afterProductDiscount + baseLine.taxWithoutPaymentDiscount),
            priceByDiscountPercentAndTax_Receipt = toClientAmount(receipt.total),
            priceByDiscountPercentAndTax_cheque = toClientAmount(cheque.total),
            priceByDiscountPercentAndTax_immediate = toClientAmount(immediate.total),
            priceDiscount = toClientAmount(baseLine.discountAmount),
            productCode = product.productCode,
            productGroupCode = product.productGroupCode,
            productImage = product.productImage.orEmpty(),
            productName = product.productName.orEmpty(),
            quantity = quantity,
            unit1 = product.unit1.orEmpty(),
            unit2 = product.unit2.orEmpty(),
            unitid1 = product.unitid1.orEmpty(),
            unitid2 = product.unitid2.orEmpty()
        )
    }

    private fun calculateLine(
        product: Product,
        quantity: Int,
        paymentTerm: PaymentTerm?,
        kind: PaymentKind = PaymentKind.RECEIPT
    ): LineCalculation {
        if (quantity <= 0) throw BadRequestException("تعداد کالا باید بزرگ‌تر از صفر باشد")
        if (product.price < 0) throw BadRequestException("قیمت کالا معتبر نیست")

        val gross = product.price.toLong() * quantity.toLong()
        val productDiscountPercent = bestDiscountPercent(product, quantity)
        val discountAmount = percent(gross, productDiscountPercent)
        val afterProductDiscount = gross - discountAmount

        val paymentDiscountPercent = when (kind) {
            PaymentKind.IMMEDIATE -> paymentTerm?.immediateDiscountPercent ?: 0
            PaymentKind.CHEQUE -> paymentTerm?.chequeDiscountPercent ?: 0
            PaymentKind.RECEIPT -> paymentTerm?.receiptDiscountPercent ?: 0
        }

        val paymentDiscountAmount = percent(afterProductDiscount, paymentDiscountPercent)
        val taxable = afterProductDiscount - paymentDiscountAmount
        val taxAmount = if (product.isTax) percent(taxable, taxPercent) else 0L
        val taxWithoutPaymentDiscount = if (product.isTax) percent(afterProductDiscount, taxPercent) else 0L
        val total = taxable + taxAmount

        return LineCalculation(
            gross = gross,
            productDiscountPercent = productDiscountPercent,
            discountAmount = discountAmount,
            afterProductDiscount = afterProductDiscount,
            paymentDiscountAmount = paymentDiscountAmount,
            taxAmount = taxAmount,
            taxWithoutPaymentDiscount = taxWithoutPaymentDiscount,
            total = total
        )
    }

    private fun bestDiscountPercent(product: Product, quantity: Int): Int {
        val productId = product.id ?: return 0

        if (!product.isDiscounts) return 0

        return discountRepository.findByProductId(productId)
            .filter { quantity in it.fromNumber..it.endNumber }
            .maxByOrNull { it.discountPercent }
            ?.discountPercent ?: 0
    }

    private fun validateSameHeader(
        requests: List<InsertCartModelRequest>,
        first: InsertCartModelRequest
    ) {
        requests.forEach {
            if (it.customerAddressId != first.customerAddressId) {
                throw BadRequestException("همه آیتم‌های سبد باید یک آدرس مشترک داشته باشند")
            }
            if (it.paymentTermId != first.paymentTermId) {
                throw BadRequestException("همه آیتم‌های سبد باید یک روش پرداخت مشترک داشته باشند")
            }
            if (it.quantity <= 0) {
                throw BadRequestException("تعداد کالا باید بزرگ‌تر از صفر باشد")
            }
        }
    }

    private fun percent(amount: Long, percent: Int): Long {
        if (percent <= 0) return 0
        if (percent > 100) throw BadRequestException("درصد معتبر نیست")

        return BigDecimal.valueOf(amount)
            .multiply(BigDecimal.valueOf(percent.toLong()))
            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
            .toLong()
    }

    private fun toPersistedAmount(value: Long): Int {
        if (value < 0 || value > Int.MAX_VALUE) {
            throw BadRequestException("مبلغ سفارش بیش از حد مجاز است")
        }
        return value.toInt()
    }

    private fun toClientAmount(value: Long): Int = toPersistedAmount(value)

    private fun nextCartCode(): Int =
        (entityManager.createNativeQuery("select nextval('cart_code_seq')").singleResult as Number).toInt()

    private fun String.toUuidOrBadRequest(message: String): UUID =
        runCatching { UUID.fromString(trim()) }
            .getOrElse { throw BadRequestException(message) }

    private fun String.toLocalDateOrNull(): LocalDate? {
        val value = trim().normalizeDigits()
        if (value.isBlank()) return null

        val normalized = value.replace('/', '-')
        val patterns = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy-M-d")
        )

        return patterns.firstNotNullOfOrNull { formatter ->
            runCatching { LocalDate.parse(normalized, formatter) }.getOrNull()
        }
    }

    private fun String.normalizeDigits(): String =
        map {
            when (it) {
                '۰' -> '0'
                '۱' -> '1'
                '۲' -> '2'
                '۳' -> '3'
                '۴' -> '4'
                '۵' -> '5'
                '۶' -> '6'
                '۷' -> '7'
                '۸' -> '8'
                '۹' -> '9'
                '٠' -> '0'
                '١' -> '1'
                '٢' -> '2'
                '٣' -> '3'
                '٤' -> '4'
                '٥' -> '5'
                '٦' -> '6'
                '٧' -> '7'
                '٨' -> '8'
                '٩' -> '9'
                else -> it
            }
        }.joinToString("")
}

private enum class PaymentKind {
    IMMEDIATE,
    CHEQUE,
    RECEIPT
}

private data class NormalizedCartLine(
    val productCode: Int,
    val quantity: Int
)

private data class LineCalculation(
    val gross: Long,
    val productDiscountPercent: Int,
    val discountAmount: Long,
    val afterProductDiscount: Long,
    val paymentDiscountAmount: Long,
    val taxAmount: Long,
    val taxWithoutPaymentDiscount: Long,
    val total: Long
)