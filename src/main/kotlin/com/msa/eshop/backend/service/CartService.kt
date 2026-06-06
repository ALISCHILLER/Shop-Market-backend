package com.msa.eshop.backend.service

import com.msa.eshop.backend.common.dtos.CartCheckoutRequest
import com.msa.eshop.backend.common.dtos.CartCheckoutResponse
import com.msa.eshop.backend.common.dtos.CartDetailsDto
import com.msa.eshop.backend.common.dtos.CartHistoryDto
import com.msa.eshop.backend.common.dtos.CartSimulateRequest
import com.msa.eshop.backend.common.dtos.CartSimulateResponse
import com.msa.eshop.backend.common.dtos.OrderAddressDto
import com.msa.eshop.backend.common.dtos.PaymentTermDto
import com.msa.eshop.backend.service.cart.CartCheckoutService
import com.msa.eshop.backend.service.cart.CartQueryService
import org.springframework.stereotype.Service

@Service
class CartService(
    private val cartQueryService: CartQueryService,
    private val cartCheckoutService: CartCheckoutService
) {
    fun simulate(request: CartSimulateRequest): CartSimulateResponse =
        cartQueryService.simulate(request)

    fun checkout(request: CartCheckoutRequest): CartCheckoutResponse =
        cartCheckoutService.checkout(request)

    fun currentCustomerAddresses(): List<OrderAddressDto> =
        cartQueryService.currentCustomerAddresses()

    fun paymentTerms(): List<PaymentTermDto> =
        cartQueryService.paymentTerms()

    fun history(
        fromDate: String?,
        toDate: String?
    ): List<CartHistoryDto> =
        cartQueryService.history(
            fromDate = fromDate,
            toDate = toDate
        )

    fun details(cartCode: Int): CartDetailsDto =
        cartQueryService.details(cartCode)

    fun checkout(
        request: CartCheckoutRequest,
        idempotencyKey: String?
    ): CartCheckoutResponse =
        cartCheckoutService.checkout(
            request = request,
            idempotencyKey = idempotencyKey
        )

}