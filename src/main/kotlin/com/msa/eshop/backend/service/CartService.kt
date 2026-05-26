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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.roundToInt

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
        return requests.map { request ->
            val product = productRepository.findByProductCode(request.productCode)
                ?: throw NotFoundException("کالا با کد ${request.productCode} پیدا نشد")
            calculateSimulate(product, request.quantity, paymentTerm)
        }
    }

    @Transactional(readOnly = true)
    fun currentCustomerAddresses(): List<OrderAddressDto> {
        val customer = currentUserService.requireCustomer()
        return addressRepository.findByCustomerId(requireNotNull(customer.id)).map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun paymentTerms(): List<PaymentTermDto> = paymentTermRepository.findByActiveTrueOrderByDeadLineAsc().map { it.toDto() }

    @Transactional
    fun insertCart(requests: List<InsertCartModelRequest>): Boolean {
        if (requests.isEmpty()) throw BadRequestException("سبد خرید خالی است")

        val customer = currentUserService.requireCustomer()
        val first = requests.first()
        val addressId = first.customerAddressId.toUuidOrBadRequest("شناسه آدرس معتبر نیست")
        val paymentTermId = first.paymentTermId.toUuidOrBadRequest("شناسه روش پرداخت معتبر نیست")

        val address = addressRepository.findById(addressId).orElseThrow { NotFoundException("آدرس سفارش پیدا نشد") }
        if (address.customer?.id != customer.id) {
            throw BadRequestException("آدرس انتخاب‌شده متعلق به این مشتری نیست")
        }
        val paymentTerm = paymentTermRepository.findById(paymentTermId).orElseThrow { NotFoundException("روش پرداخت پیدا نشد") }
        val cartCode = nextCartCode()

        val cart = Cart(
            cartCode = cartCode,
            customer = customer,
            address = address,
            paymentTerm = paymentTerm,
            customerNameSnapshot = customer.customerName,
            customerAddressSnapshot = address.customerAddress,
            statusName = "ثبت شده",
            statusColor = "#2E7D32",
            salesDate = LocalDate.now()
        )

        requests.forEach { request ->
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
                    discount = line.discountAmount + line.paymentDiscountAmount,
                    tax = line.taxAmount,
                    total = line.total
                )
            )
            cart.subtotal += line.gross
            cart.discountTotal += line.discountAmount + line.paymentDiscountAmount
            cart.taxTotal += line.taxAmount
            cart.total += line.total
        }

        cartRepository.save(cart)
        return true
    }

    @Transactional(readOnly = true)
    fun history(request: ReportHistoryCustomerModelRequest): List<ReportHistoryCustomerDto> {
        val current = currentUserService.requireCustomer()
        val customerId = request.customerId.trim().takeIf { it.isNotBlank() }?.toUuidOrBadRequest("شناسه مشتری معتبر نیست")
            ?: requireNotNull(current.id)

        if (customerId != current.id && current.role.uppercase() != "ADMIN") {
            throw BadRequestException("دسترسی به گزارش این مشتری مجاز نیست")
        }

        val fromDate = request.fromDate.toLocalDateOrNull()
        val toDate = request.endDate.toLocalDateOrNull()
        val customer = customerRepository.findById(customerId).orElseThrow { NotFoundException("مشتری پیدا نشد") }
        return cartRepository.findHistory(requireNotNull(customer.id), fromDate, toDate).map { it.toHistoryDto() }
    }

    @Transactional(readOnly = true)
    fun details(cartCode: Int): List<ReportCartDetailsDto> {
        val current = currentUserService.requireCustomer()
        val cart = cartRepository.findByCartCode(cartCode) ?: throw NotFoundException("سفارش پیدا نشد")
        if (cart.customer?.id != current.id && current.role.uppercase() != "ADMIN") {
            throw BadRequestException("دسترسی به جزئیات این سفارش مجاز نیست")
        }
        return cart.items.sortedBy { it.id.toString() }.map { it.toDetailsDto(cart) }
    }

    private fun calculateSimulate(product: Product, quantity: Int, paymentTerm: PaymentTerm?): SimulateDto {
        val baseLine = calculateLine(product, quantity, paymentTerm)
        val immediate = calculateLine(product, quantity, paymentTerm, PaymentKind.IMMEDIATE)
        val cheque = calculateLine(product, quantity, paymentTerm, PaymentKind.CHEQUE)
        val receipt = calculateLine(product, quantity, paymentTerm, PaymentKind.RECEIPT)

        return SimulateDto(
            convertFactor1 = product.convertFactor1,
            convertFactor2 = product.convertFactor2,
            discountPercent = baseLine.productDiscountPercent,
            discount_Percent_PaymentTerm_Receipt = receipt.paymentDiscountAmount,
            discount_Percent_PaymentTerm_Receipt_Tax = receipt.taxAmount,
            discount_Percent_PaymentTerm_cheque = cheque.paymentDiscountAmount,
            discount_Percent_PaymentTerm_cheque_Tax = cheque.taxAmount,
            discount_Percent_PaymentTerm_immediate = immediate.paymentDiscountAmount,
            discount_Percent_PaymentTerm_immediate_Tax = immediate.taxAmount,
            finalPrice = baseLine.gross,
            finalPriceDiscount = baseLine.afterProductDiscount,
            fullNameKala1 = product.fullNameKala1.orEmpty(),
            fullNameKala2 = product.fullNameKala2.orEmpty(),
            id = requireNotNull(product.id).toString(),
            isTax = product.isTax,
            paymentTermId = paymentTerm?.id?.toString(),
            price = product.price,
            priceByDiscountPercent = baseLine.afterProductDiscount,
            priceByDiscountPercentAndTax = baseLine.afterProductDiscount + baseLine.taxWithoutPaymentDiscount,
            priceByDiscountPercentAndTax_Receipt = receipt.total,
            priceByDiscountPercentAndTax_cheque = cheque.total,
            priceByDiscountPercentAndTax_immediate = immediate.total,
            priceDiscount = baseLine.discountAmount,
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
        val gross = safeInt(product.price.toLong() * quantity.toLong())
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
        val taxAmount = if (product.isTax) percent(taxable, taxPercent) else 0
        val taxWithoutPaymentDiscount = if (product.isTax) percent(afterProductDiscount, taxPercent) else 0
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
        return discountRepository.findByProductId(productId)
            .filter { quantity in it.fromNumber..it.endNumber }
            .maxByOrNull { it.discountPercent }
            ?.discountPercent ?: 0
    }

    private fun percent(amount: Int, percent: Int): Int = ((amount.toDouble() * percent.toDouble()) / 100.0).roundToInt()

    private fun safeInt(value: Long): Int = value.coerceIn(0, Int.MAX_VALUE.toLong()).toInt()

    private fun nextCartCode(): Int = (entityManager
        .createNativeQuery("select nextval('cart_code_seq')")
        .singleResult as Number).toInt()

    private fun String.toUuidOrBadRequest(message: String): UUID = runCatching { UUID.fromString(this) }
        .getOrElse { throw BadRequestException(message) }

    private fun String.toLocalDateOrNull(): LocalDate? {
        val value = trim()
        if (value.isBlank()) return null
        val normalized = value.replace('/', '-')
        val patterns = listOf(DateTimeFormatter.ISO_LOCAL_DATE, DateTimeFormatter.ofPattern("yyyy-M-d"))
        return patterns.firstNotNullOfOrNull { formatter -> runCatching { LocalDate.parse(normalized, formatter) }.getOrNull() }
    }
}

private enum class PaymentKind { IMMEDIATE, CHEQUE, RECEIPT }

private data class LineCalculation(
    val gross: Int,
    val productDiscountPercent: Int,
    val discountAmount: Int,
    val afterProductDiscount: Int,
    val paymentDiscountAmount: Int,
    val taxAmount: Int,
    val taxWithoutPaymentDiscount: Int,
    val total: Int
)
