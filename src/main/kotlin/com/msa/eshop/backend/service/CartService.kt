package com.msa.eshop.backend.service

import com.msa.eshop.backend.common.InsertCartModelRequest
import com.msa.eshop.backend.common.OrderAddressDto
import com.msa.eshop.backend.common.PaymentTermDto
import com.msa.eshop.backend.common.ReportCartDetailsDto
import com.msa.eshop.backend.common.ReportHistoryCustomerDto
import com.msa.eshop.backend.common.ReportHistoryCustomerModelRequest
import com.msa.eshop.backend.common.SimulateDto
import com.msa.eshop.backend.common.SimulateModelRequest
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