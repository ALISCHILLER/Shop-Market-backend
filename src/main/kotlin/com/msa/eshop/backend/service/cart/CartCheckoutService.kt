package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.dtos.CartCheckoutRequest
import com.msa.eshop.backend.common.dtos.CartCheckoutResponse
import com.msa.eshop.backend.domain.entity.CartIdempotencyKey
import com.msa.eshop.backend.domain.repository.CartRepository
import com.msa.eshop.backend.domain.repository.CustomerAddressRepository
import com.msa.eshop.backend.service.CurrentUserService
import com.msa.eshop.backend.service.stock.StockReservationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartCheckoutService(
    private val currentUserService: CurrentUserService,
    private val addressRepository: CustomerAddressRepository,
    private val cartRepository: CartRepository,
    private val cartCodeGenerator: CartCodeGenerator,
    private val cartLineNormalizer: CartLineNormalizer,
    private val cartPricingCalculator: CartPricingCalculator,
    private val cartAssembler: CartAssembler,
    private val cartIdempotencyService: CartIdempotencyService,
    private val stockReservationService: StockReservationService
) {

    @Transactional
    fun checkout(
        request: CartCheckoutRequest,
        idempotencyKey: String?
    ): CartCheckoutResponse {
        val currentCustomer = currentUserService.requireCustomer()

        val normalizedIdempotencyKey = cartIdempotencyService.normalizeKey(idempotencyKey)
        val requestHash = normalizedIdempotencyKey?.let {
            cartIdempotencyService.requestHash(request)
        }

        if (normalizedIdempotencyKey != null && requestHash != null) {
            val existing = cartIdempotencyService.findExisting(
                customer = currentCustomer,
                idempotencyKey = normalizedIdempotencyKey,
                requestHash = requestHash
            )

            if (existing != null) {
                return existing.response
            }
        }

        val idempotencyRecord = createIdempotencyRecordIfNeeded(
            customer = currentCustomer,
            normalizedIdempotencyKey = normalizedIdempotencyKey,
            requestHash = requestHash
        )

        try {
            val address = addressRepository.findById(request.customerAddressId)
                .orElseThrow { NotFoundException("آدرس سفارش پیدا نشد") }

            if (address.customer?.id != currentCustomer.id) {
                throw BadRequestException("آدرس انتخاب‌شده متعلق به این مشتری نیست")
            }

            val lines = cartLineNormalizer.normalize(request.items)

            val pricingResult = cartPricingCalculator.calculate(
                CartPricingRequest(
                    paymentTermId = request.paymentTermId,
                    lines = lines
                )
            )

            val cart = cartAssembler.assemble(
                cartCode = cartCodeGenerator.next(),
                customer = currentCustomer,
                address = address,
                pricingResult = pricingResult
            )

            val savedCart = cartRepository.save(cart)

            stockReservationService.reserveForCart(
                cart = savedCart,
                lines = lines
            )

            cartIdempotencyService.complete(
                record = idempotencyRecord,
                cart = savedCart
            )

            return savedCart.toCheckoutResponse(normalizedIdempotencyKey)
        } catch (ex: RuntimeException) {
            cartIdempotencyService.fail(idempotencyRecord)
            throw ex
        }
    }

    private fun createIdempotencyRecordIfNeeded(
        customer: com.msa.eshop.backend.domain.entity.Customer,
        normalizedIdempotencyKey: String?,
        requestHash: String?
    ): CartIdempotencyKey? {
        if (normalizedIdempotencyKey == null || requestHash == null) {
            return null
        }

        return cartIdempotencyService.createProcessing(
            customer = customer,
            idempotencyKey = normalizedIdempotencyKey,
            requestHash = requestHash
        )
    }

    private fun com.msa.eshop.backend.domain.entity.Cart.toCheckoutResponse(
        idempotencyKey: String?
    ): CartCheckoutResponse =
        CartCheckoutResponse(
            cartId = requireNotNull(id),
            cartCode = cartCode,
            statusCode = statusCode,
            statusName = statusName,
            subtotal = subtotal,
            discountTotal = discountTotal,
            taxTotal = taxTotal,
            total = total,
            idempotencyKey = idempotencyKey
        )
}