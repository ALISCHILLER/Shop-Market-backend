package com.msa.eshop.backend.service

import com.msa.eshop.backend.common.dtos.CartSimulateRequest
import com.msa.eshop.backend.common.dtos.CartSimulateResponse
import com.msa.eshop.backend.common.dtos.InsertCartModelRequest
import com.msa.eshop.backend.common.dtos.OrderAddressDto
import com.msa.eshop.backend.common.dtos.PaymentTermDto
import com.msa.eshop.backend.common.dtos.ReportCartDetailsDto
import com.msa.eshop.backend.common.dtos.ReportHistoryCustomerDto
import com.msa.eshop.backend.common.dtos.ReportHistoryCustomerModelRequest
import com.msa.eshop.backend.common.dtos.SimulateDto
import com.msa.eshop.backend.common.dtos.SimulateModelRequest
import com.msa.eshop.backend.service.cart.CartCheckoutService
import com.msa.eshop.backend.service.cart.CartQueryService
import org.springframework.stereotype.Service

@Service
class CartService(
    private val cartQueryService: CartQueryService,
    private val cartCheckoutService: CartCheckoutService
) {
    fun simulate(requests: List<SimulateModelRequest>): List<SimulateDto> =
        cartQueryService.simulate(requests)

    fun simulateModern(request: CartSimulateRequest): CartSimulateResponse =
        cartQueryService.simulateModern(request)

    fun currentCustomerAddresses(): List<OrderAddressDto> =
        cartQueryService.currentCustomerAddresses()

    fun paymentTerms(): List<PaymentTermDto> =
        cartQueryService.paymentTerms()

    fun insertCart(requests: List<InsertCartModelRequest>): Boolean =
        cartCheckoutService.checkout(requests)

    fun history(request: ReportHistoryCustomerModelRequest): List<ReportHistoryCustomerDto> =
        cartQueryService.history(request)

    fun details(cartCode: Int): List<ReportCartDetailsDto> =
        cartQueryService.details(cartCode)
}