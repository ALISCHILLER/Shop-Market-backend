package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
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
import com.msa.eshop.backend.domain.CartRepository
import com.msa.eshop.backend.domain.CustomerAddress
import com.msa.eshop.backend.domain.CustomerAddressRepository
import com.msa.eshop.backend.domain.CustomerRepository
import com.msa.eshop.backend.domain.PaymentTermRepository
import com.msa.eshop.backend.domain.Product
import com.msa.eshop.backend.domain.ProductRepository
import com.msa.eshop.backend.service.CurrentUserService
import com.msa.eshop.backend.service.PricingService
import com.msa.eshop.backend.service.toDetailsDto
import com.msa.eshop.backend.service.toDto
import com.msa.eshop.backend.service.toHistoryDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartQueryService(
    private val currentUserService: CurrentUserService,
    private val customerRepository: CustomerRepository,
    private val addressRepository: CustomerAddressRepository,
    private val paymentTermRepository: PaymentTermRepository,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val pricingService: PricingService,
    private val cartAccessPolicy: CartAccessPolicy,
    private val cartLineNormalizer: CartLineNormalizer
) {
    @Transactional(readOnly = true)
    fun simulate(requests: List<SimulateModelRequest>): List<SimulateDto> {
        val lines = cartLineNormalizer.normalizeSimulateLines(requests)
        if (lines.isEmpty()) return emptyList()

        val paymentTerm = paymentTermRepository.findFirstByActiveTrueOrderByDeadLineAsc()
        val products = loadProducts(lines.map { it.productCode })

        return lines.map { line ->
            val product = products[line.productCode]
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
        val customerId = requireNotNull(customer.id)

        return addressRepository.findByCustomerId(customerId)
            .sortedWith(
                compareByDescending<CustomerAddress> { it.isDefault }
                    .thenBy { it.centerName }
            )
            .map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun paymentTerms(): List<PaymentTermDto> =
        paymentTermRepository.findByActiveTrueOrderByDeadLineAsc()
            .map { it.toDto() }

    @Transactional(readOnly = true)
    fun history(request: ReportHistoryCustomerModelRequest): List<ReportHistoryCustomerDto> {
        val current = currentUserService.requireCustomer()

        val targetCustomerId = request.customerId
            .trim()
            .takeIf { it.isNotBlank() }
            ?.toUuidOrBadRequest("شناسه مشتری معتبر نیست")
            ?: requireNotNull(current.id)

        cartAccessPolicy.assertCanReadCustomerHistory(current, targetCustomerId)

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
        if (cartCode <= 0) {
            throw BadRequestException("کد سفارش معتبر نیست")
        }

        val current = currentUserService.requireCustomer()

        val cart = cartRepository.findByCartCode(cartCode)
            ?: throw NotFoundException("سفارش پیدا نشد")

        cartAccessPolicy.assertCanReadCart(current, cart)

        return cart.items
            .sortedBy { it.productCode }
            .map { it.toDetailsDto(cart) }
    }

    private fun loadProducts(productCodes: List<Int>): Map<Int, Product> {
        val uniqueCodes = productCodes.toSet()

        val products = productRepository.findByProductCodeIn(uniqueCodes)
            .associateBy { it.productCode }

        val missingCodes = uniqueCodes - products.keys
        if (missingCodes.isNotEmpty()) {
            throw NotFoundException("کالا با کد ${missingCodes.first()} پیدا نشد")
        }

        return products
    }
}