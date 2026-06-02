package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.dtos.CartDetailsDto
import com.msa.eshop.backend.common.dtos.CartHistoryDto
import com.msa.eshop.backend.common.dtos.CartSimulateRequest
import com.msa.eshop.backend.common.dtos.CartSimulateResponse
import com.msa.eshop.backend.common.dtos.OrderAddressDto
import com.msa.eshop.backend.common.dtos.PaymentTermDto
import com.msa.eshop.backend.common.parseClientDateOrNull
import com.msa.eshop.backend.domain.entity.CustomerAddress
import com.msa.eshop.backend.domain.repository.CartRepository
import com.msa.eshop.backend.domain.repository.CustomerAddressRepository
import com.msa.eshop.backend.domain.repository.PaymentTermRepository
import com.msa.eshop.backend.service.CurrentUserService
import com.msa.eshop.backend.service.toDetailsDto
import com.msa.eshop.backend.service.toDto
import com.msa.eshop.backend.service.toHistoryDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartQueryService(
    private val currentUserService: CurrentUserService,
    private val addressRepository: CustomerAddressRepository,
    private val paymentTermRepository: PaymentTermRepository,
    private val cartRepository: CartRepository,
    private val cartAccessPolicy: CartAccessPolicy,
    private val cartLineNormalizer: CartLineNormalizer,
    private val cartPricingCalculator: CartPricingCalculator
) {

    @Transactional(readOnly = true)
    fun simulate(request: CartSimulateRequest): CartSimulateResponse {
        val normalizedLines = cartLineNormalizer.normalize(request.items)

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
    fun history(
        fromDate: String?,
        toDate: String?
    ): List<CartHistoryDto> {
        val current = currentUserService.requireCustomer()
        val customerId = requireNotNull(current.id)

        val parsedFromDate = fromDate.parseClientDateOrNull()
        val parsedToDate = toDate.parseClientDateOrNull()

        if (parsedFromDate != null && parsedToDate != null && parsedFromDate.isAfter(parsedToDate)) {
            throw BadRequestException("تاریخ شروع نمی‌تواند بعد از تاریخ پایان باشد")
        }

        return cartRepository.findHistory(
            customerId = customerId,
            fromDate = parsedFromDate,
            toDate = parsedToDate
        ).map { it.toHistoryDto() }
    }

    @Transactional(readOnly = true)
    fun details(cartCode: Int): CartDetailsDto {
        if (cartCode <= 0) {
            throw BadRequestException("کد سفارش معتبر نیست")
        }

        val current = currentUserService.requireCustomer()

        val cart = cartRepository.findByCartCode(cartCode)
            ?: throw NotFoundException("سفارش پیدا نشد")

        cartAccessPolicy.assertCanReadCart(current, cart)

        return cart.toDetailsDto()
    }
}