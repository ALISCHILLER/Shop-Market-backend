package com.msa.eshop.backend.api

import com.msa.eshop.backend.common.SimulateResultModel
import com.msa.eshop.backend.common.dtos.InsertCartModelRequest
import com.msa.eshop.backend.common.InsertCartModelResponse
import com.msa.eshop.backend.common.PaymentTermResponse
import com.msa.eshop.backend.common.ReportCartDetailsResponse
import com.msa.eshop.backend.common.dtos.ReportHistoryCustomerModelRequest
import com.msa.eshop.backend.common.ReportHistoryCustomerResponse
import com.msa.eshop.backend.common.dtos.SimulateModelRequest
import com.msa.eshop.backend.service.CartService
import jakarta.validation.Valid
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
    fun simulate(
        @Valid @RequestBody request: List<@Valid SimulateModelRequest>
    ): SimulateResultModel =
        SimulateResultModel(cartService.simulate(request))

    @GetMapping("/GetPaymentTerm")
    fun paymentTerms(): PaymentTermResponse =
        PaymentTermResponse(cartService.paymentTerms())

    @PostMapping("/InsertCart")
    fun insertCart(
        @Valid @RequestBody request: List<@Valid InsertCartModelRequest>
    ): InsertCartModelResponse =
        InsertCartModelResponse(cartService.insertCart(request))

    @PostMapping("/ReportHistoryCustomer")
    fun history(
        @RequestBody request: ReportHistoryCustomerModelRequest
    ): ReportHistoryCustomerResponse =
        ReportHistoryCustomerResponse(cartService.history(request))

    @GetMapping("/ReportCartDetails")
    fun details(
        @RequestParam("CartCode") cartCode: Int
    ): ReportCartDetailsResponse =
        ReportCartDetailsResponse(cartService.details(cartCode))
}