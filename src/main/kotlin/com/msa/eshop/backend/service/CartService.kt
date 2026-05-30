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
import com.msa.eshop.backend.common.parseClientDateOrNull
import com.msa.eshop.backend.common.toUuidOrBadRequest
import com.msa.eshop.backend.domain.Cart
import com.msa.eshop.backend.domain.CartItem
import com.msa.eshop.backend.domain.CartRepository
import com.msa.eshop.backend.domain.CartStatus
import com.msa.eshop.backend.domain.CustomerAddressRepository
import com.msa.eshop.backend.domain.CustomerRepository
import com.msa.eshop.backend.domain.PaymentTermRepository
import com.msa.eshop.backend.domain.ProductRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
class CartService(
    private val currentUserService: CurrentUserService,
    private val customerRepository: CustomerRepository,
    private val addressRepository: CustomerAddressRepository,
    private val productRepository: ProductRepository,
    private val paymentTermRepository: PaymentTermRepository,
    private val cartRepository: CartRepository,
    private val pricingService: PricingService,
    private val entityManager: EntityManager
) {
    @Transactional(readOnly = true)
    fun simulate(requests: List<SimulateModelRequest>): List<SimulateDto> {
        if (requests.isEmpty()) return emptyList()

        val normalizedLines = requests.normalizeSimulateLines()
        val paymentTerm = paymentTermRepository.findFirstByActiveTrueOrderByDeadLineAsc()

        return normalizedLines.map { line ->
            val product = productRepository.findByProductCode(line.productCode)
                ?: throw NotFoundException("کالا با کد ${line.productCode} پیدا نشد")

            pricingService.simulate(
                product = product,
                quantity = line.quantity,
                paymentTerm = paymentTerm
            )
        }
    }

    @Transactional(readOnly = true)
    fun currentCustomerAddresses(): List<OrderAddressDto> {
        val customer = currentUserService.requireCustomer()

        return addressRepository.findByCustomerId(requireNotNull(customer.id))
            .sortedWith(
                compareByDescending<com.msa.eshop.backend.domain.CustomerAddress> { it.isDefault }
                    .thenBy { it.centerName }
            )
            .map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun paymentTerms(): List<PaymentTermDto> =
        paymentTermRepository.findByActiveTrueOrderByDeadLineAsc()
            .map { it.toDto() }

    @Transactional
    fun insertCart(requests: List<InsertCartModelRequest>): Boolean {
        if (requests.isEmpty()) throw BadRequestException("سبد خرید خالی است")

        val currentCustomer = currentUserService.requireCustomer()
        val first = requests.first()

        requests.validateSameHeader(first)

        val addressId = first.customerAddressId.toUuidOrBadRequest("شناسه آدرس معتبر نیست")
        val paymentTermId = first.paymentTermId.toUuidOrBadRequest("شناسه روش پرداخت معتبر نیست")

        val address = addressRepository.findById(addressId)
            .orElseThrow { NotFoundException("آدرس سفارش پیدا نشد") }

        if (address.customer?.id != currentCustomer.id) {
            throw BadRequestException("آدرس انتخاب‌شده متعلق به این مشتری نیست")
        }

        val paymentTerm = paymentTermRepository.findById(paymentTermId)
            .orElseThrow { NotFoundException("روش پرداخت پیدا نشد") }

        if (!paymentTerm.active) {
            throw BadRequestException("روش پرداخت انتخاب‌شده غیرفعال است")
        }

        val normalizedLines = requests.normalizeCartLines()

        val status = CartStatus.REGISTERED

        val cart = Cart(
            cartCode = nextCartCode(),
            customer = currentCustomer,
            address = address,
            paymentTerm = paymentTerm,
            customerNameSnapshot = currentCustomer.customerName,
            customerAddressSnapshot = address.customerAddress,
            statusName = status.title,
            statusColor = status.color,
            salesDate = LocalDate.now()
        )

        normalizedLines.forEach { requestLine ->
            val product = productRepository.findByProductCode(requestLine.productCode)
                ?: throw NotFoundException("کالا با کد ${requestLine.productCode} پیدا نشد")

            val priceLine = pricingService.calculate(
                product = product,
                quantity = requestLine.quantity,
                paymentTerm = paymentTerm
            )

            cart.addItem(
                CartItem(
                    product = product,
                    productCode = product.productCode,
                    productName = product.productName.orEmpty(),
                    productImageUrl = product.productImage,
                    quantity = requestLine.quantity,
                    price = product.price,
                    discount = priceLine.totalDiscount.toPersistedInt(),
                    tax = priceLine.tax.toPersistedInt(),
                    total = priceLine.total.toPersistedInt()
                )
            )

            cart.subtotal += priceLine.gross.toPersistedInt()
            cart.discountTotal += priceLine.totalDiscount.toPersistedInt()
            cart.taxTotal += priceLine.tax.toPersistedInt()
            cart.total += priceLine.total.toPersistedInt()
        }

        cartRepository.save(cart)
        return true
    }

    @Transactional(readOnly = true)
    fun history(request: ReportHistoryCustomerModelRequest): List<ReportHistoryCustomerDto> {
        val current = currentUserService.requireCustomer()

        val requestedCustomerId = request.customerId.trim()
            .takeIf { it.isNotBlank() }
            ?.toUuidOrBadRequest("شناسه مشتری معتبر نیست")

        val targetCustomerId = requestedCustomerId ?: requireNotNull(current.id)

        val isAdmin = current.role.equals("ADMIN", ignoreCase = true)
        if (!isAdmin && targetCustomerId != current.id) {
            throw BadRequestException("دسترسی به گزارش این مشتری مجاز نیست")
        }

        val fromDate = request.fromDate.parseClientDateOrNull()
        val toDate = request.endDate.parseClientDateOrNull()

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw BadRequestException("تاریخ شروع نمی‌تواند بعد از تاریخ پایان باشد")
        }

        val customer = customerRepository.findById(targetCustomerId)
            .orElseThrow { NotFoundException("مشتری پیدا نشد") }

        return cartRepository.findHistory(
            customerId = requireNotNull(customer.id),
            fromDate = fromDate,
            toDate = toDate
        ).map { it.toHistoryDto() }
    }

    @Transactional(readOnly = true)
    fun details(cartCode: Int): List<ReportCartDetailsDto> {
        if (cartCode <= 0) throw BadRequestException("کد سفارش معتبر نیست")

        val current = currentUserService.requireCustomer()

        val cart = cartRepository.findByCartCode(cartCode)
            ?: throw NotFoundException("سفارش پیدا نشد")

        val isAdmin = current.role.equals("ADMIN", ignoreCase = true)
        if (!isAdmin && cart.customer?.id != current.id) {
            throw BadRequestException("دسترسی به جزئیات این سفارش مجاز نیست")
        }

        return cart.items
            .sortedBy { it.productCode }
            .map { it.toDetailsDto(cart) }
    }

    private fun nextCartCode(): Int =
        (entityManager.createNativeQuery("select nextval('cart_code_seq')").singleResult as Number).toInt()

    private fun List<SimulateModelRequest>.normalizeSimulateLines(): List<NormalizedLine> =
        groupBy { it.productCode }
            .map { (productCode, rows) ->
                val quantity = rows.sumOf { it.quantity }
                if (productCode <= 0) throw BadRequestException("کد کالا معتبر نیست")
                if (quantity <= 0) throw BadRequestException("تعداد کالا باید بزرگ‌تر از صفر باشد")

                NormalizedLine(
                    productCode = productCode,
                    quantity = quantity
                )
            }

    private fun List<InsertCartModelRequest>.normalizeCartLines(): List<NormalizedLine> =
        groupBy { it.productCode }
            .map { (productCode, rows) ->
                val quantity = rows.sumOf { it.quantity }
                if (productCode <= 0) throw BadRequestException("کد کالا معتبر نیست")
                if (quantity <= 0) throw BadRequestException("تعداد کالا باید بزرگ‌تر از صفر باشد")

                NormalizedLine(
                    productCode = productCode,
                    quantity = quantity
                )
            }

    private fun List<InsertCartModelRequest>.validateSameHeader(first: InsertCartModelRequest) {
        forEach {
            if (it.customerAddressId.trim() != first.customerAddressId.trim()) {
                throw BadRequestException("همه آیتم‌های سبد باید یک آدرس مشترک داشته باشند")
            }

            if (it.paymentTermId.trim() != first.paymentTermId.trim()) {
                throw BadRequestException("همه آیتم‌های سبد باید یک روش پرداخت مشترک داشته باشند")
            }

            if (it.quantity <= 0) {
                throw BadRequestException("تعداد کالا باید بزرگ‌تر از صفر باشد")
            }

            if (it.productCode <= 0) {
                throw BadRequestException("کد کالا معتبر نیست")
            }
        }
    }
}

private data class NormalizedLine(
    val productCode: Int,
    val quantity: Int
)