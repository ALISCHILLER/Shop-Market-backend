package com.msa.eshop.backend.api

import com.msa.eshop.backend.common.InsertCartModelRequest
import com.msa.eshop.backend.common.InsertCartModelResponse
import com.msa.eshop.backend.common.OrderAddressResultModel
import com.msa.eshop.backend.common.PaymentTermResponse
import com.msa.eshop.backend.common.ReportCartDetailsResponse
import com.msa.eshop.backend.common.ReportHistoryCustomerModelRequest
import com.msa.eshop.backend.common.ReportHistoryCustomerResponse
import com.msa.eshop.backend.common.SimulateModelRequest
import com.msa.eshop.backend.common.SimulateResultModel
import com.msa.eshop.backend.service.CartService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/Cart")
@Validated
class CartController(
    private val cartService: CartService
) {
    @PostMapping("/GetCartSimulateRsult")
    fun simulate(@RequestBody request: List<SimulateModelRequest>): SimulateResultModel =
        SimulateResultModel(cartService.simulate(request))

    @GetMapping("/GetPaymentTerm")
    fun paymentTerms(): PaymentTermResponse = PaymentTermResponse(cartService.paymentTerms())

    @PostMapping("/InsertCart")
    fun insertCart(@RequestBody request: List<InsertCartModelRequest>): InsertCartModelResponse =
        InsertCartModelResponse(cartService.insertCart(request))

    @PostMapping("/ReportHistoryCustomer")
    fun history(@RequestBody request: ReportHistoryCustomerModelRequest): ReportHistoryCustomerResponse =
        ReportHistoryCustomerResponse(cartService.history(request))

    @GetMapping("/ReportCartDetails")
    fun details(@RequestParam("CartCode") cartCode: Int): ReportCartDetailsResponse =
        ReportCartDetailsResponse(cartService.details(cartCode))
}
