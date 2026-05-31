package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.dtos.OrderAddressDto
import com.msa.eshop.backend.common.dtos.PaymentTermDto
import com.msa.eshop.backend.common.dtos.ReportCartDetailsDto
import com.msa.eshop.backend.common.dtos.ReportHistoryCustomerDto
import com.msa.eshop.backend.common.dtos.ReportHistoryCustomerModelRequest
import com.msa.eshop.backend.common.dtos.SimulateDto
import com.msa.eshop.backend.common.dtos.SimulateModelRequest
import com.msa.eshop.backend.common.parseClientDateOrNull
import com.msa.eshop.backend.common.toUuidOrBadRequest
import com.msa.eshop.backend.domain.CartRepository
import com.msa.eshop.backend.domain.CustomerAddress
import com.msa.eshop.backend.domain.CustomerAddressRepository
import com.msa.eshop.backend.domain.CustomerRepository
import com.msa.eshop.backend.domain.PaymentTermRepository
import com.msa.eshop.backend.service.CurrentUserService
import com.msa.eshop.backend.common.dtos.CartSimulateRequest
import com.msa.eshop.backend.common.dtos.CartSimulateResponse
import com.msa.eshop.backend.common.parseClientDateOrNull
import com.msa.eshop.backend.common.toUuidOrBadRequest
import com.msa.eshop.backend.service.PricingRequest
import com.msa.eshop.backend.service.PricingService
import com.msa.eshop.backend.service.catalog.ProductResolver
import com.msa.eshop.backend.service.toDetailsDto
import com.msa.eshop.backend.service.toDto
import com.msa.eshop.backend.service.toHistoryDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.msa.eshop.backend.service.toDetailsDto
import com.msa.eshop.backend.service.toDto
import com.msa.eshop.backend.service.toHistoryDto

@Service
class CartQueryService(
    private val currentUserService: CurrentUserService,
    private val customerRepository: CustomerRepository,
    private val addressRepository: CustomerAddressRepository,
    private val paymentTermRepository: PaymentTermRepository,
    private val cartRepository: CartRepository,
    private val cartAccessPolicy: CartAccessPolicy,
    private val cartLineNormalizer: CartLineNormalizer,
    private val cartPricingCalculator: CartPricingCalculator,
    private val productResolver: ProductResolver,
    private val pricingService: PricingService
) {
    @Transactional(readOnly = true)
    fun simulate(requests: List<SimulateModelRequest>): List<SimulateDto> {
        val header = cartLineNormalizer.extractSimulateHeader(requests)
        val lines = cartLineNormalizer.normalizeSimulateLines(requests)

        val paymentTerm = resolveLegacySimulatePaymentTerm(header)

        val productsByCode = productResolver.requireByCodes(
            lines.map { it.productCode }
        )

        val pricingRequests = lines.map { line ->
            PricingRequest(
                product = productsByCode.getValue(line.productCode),
                quantity = line.quantity
            )
        }

        return pricingService.simulateBatch(
            requests = pricingRequests,
            paymentTerm = paymentTerm
        )
    }
    @Transactional(readOnly = true)
    fun simulateModern(request: CartSimulateRequest): CartSimulateResponse {
        val lines = request.items.map {
            NormalizedCartLine(
                productCode = it.productCode,
                quantity = it.quantity
            )
        }

        val normalizedLines = normalizeModernLines(lines)

        val pricingResult = cartPricingCalculator.calculate(
            CartPricingRequest(
                paymentTermId = request.paymentTermId,
                lines = normalizedLines
            )
        )

        return pricingResult.toCartSimulateResponse()
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

    private fun pricingServiceCompatibleLegacyDtos(
        result: CartPricingResult
    ): List<SimulateDto> {
        return result.priceLines.map { line ->
            val product = line.product

            SimulateDto(
                convertFactor1 = product.convertFactor1,
                convertFactor2 = product.convertFactor2,
                discountPercent = line.productDiscountPercent,

                discount_Percent_PaymentTerm_Receipt = line.paymentDiscount.toPersistedLong(),
                discount_Percent_PaymentTerm_Receipt_Tax = line.tax.toPersistedLong(),

                discount_Percent_PaymentTerm_cheque = line.paymentDiscount.toPersistedLong(),
                discount_Percent_PaymentTerm_cheque_Tax = line.tax.toPersistedLong(),

                discount_Percent_PaymentTerm_immediate = line.paymentDiscount.toPersistedLong(),
                discount_Percent_PaymentTerm_immediate_Tax = line.tax.toPersistedLong(),

                finalPrice = line.total.toPersistedLong(),
                finalPriceDiscount = line.afterProductDiscount.toPersistedLong(),

                fullNameKala1 = product.fullNameKala1.orEmpty(),
                fullNameKala2 = product.fullNameKala2.orEmpty(),

                id = requireNotNull(product.id).toString(),
                isTax = product.isTax,

                paymentTermId = result.paymentTerm?.id?.toString(),

                price = line.gross.toPersistedLong(),
                priceByDiscountPercent = line.afterProductDiscount.toPersistedLong(),
                priceByDiscountPercentAndTax = (line.afterProductDiscount + line.taxWithoutPaymentDiscount).toPersistedLong(),

                priceByDiscountPercentAndTax_Receipt = line.total.toPersistedLong(),
                priceByDiscountPercentAndTax_cheque = line.total.toPersistedLong(),
                priceByDiscountPercentAndTax_immediate = line.total.toPersistedLong(),

                priceDiscount = line.productDiscount.toPersistedLong(),

                productCode = product.productCode,
                productGroupCode = product.productGroupCode,
                productImage = product.productImage.orEmpty(),
                productName = product.productName.orEmpty(),

                quantity = line.quantity,

                unit1 = product.unit1.orEmpty(),
                unit2 = product.unit2.orEmpty(),
                unitid1 = product.unitid1.orEmpty(),
                unitid2 = product.unitid2.orEmpty()
            )
        }
    }

    private fun normalizeModernLines(lines: List<NormalizedCartLine>): List<NormalizedCartLine> {
        if (lines.isEmpty()) {
            throw BadRequestException("سبد خرید خالی است")
        }

        return lines
            .onEach {
                if (it.productCode <= 0) {
                    throw BadRequestException("کد کالا معتبر نیست")
                }

                if (it.quantity <= 0) {
                    throw BadRequestException("تعداد کالا باید بزرگ‌تر از صفر باشد")
                }
            }
            .groupBy { it.productCode }
            .map { (productCode, rows) ->
                NormalizedCartLine(
                    productCode = productCode,
                    quantity = rows.sumOf { it.quantity }
                )
            }
            .sortedBy { it.productCode }
    }

    private fun resolveLegacySimulatePaymentTerm(header: SimulateHeader) =
        header.paymentTermId
            ?.toUuidOrBadRequest("شناسه روش پرداخت معتبر نیست")
            ?.let { paymentTermId ->
                paymentTermRepository.findByIdAndActiveTrue(paymentTermId)
                    ?: throw BadRequestException("روش پرداخت انتخاب‌شده معتبر یا فعال نیست")
            }
            ?: paymentTermRepository.findFirstByActiveTrueOrderByDeadLineAsc()
            ?: throw BadRequestException("هیچ روش پرداخت فعالی تعریف نشده است")
}