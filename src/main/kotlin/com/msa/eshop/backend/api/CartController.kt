package com.msa.eshop.backend.api

import com.msa.eshop.backend.common.BaseResponse
import com.msa.eshop.backend.common.dtos.CartCheckoutRequest
import com.msa.eshop.backend.common.dtos.CartCheckoutResponse
import com.msa.eshop.backend.common.dtos.CartDetailsDto
import com.msa.eshop.backend.common.dtos.CartHistoryDto
import com.msa.eshop.backend.common.dtos.CartSimulateRequest
import com.msa.eshop.backend.common.dtos.CartSimulateResponse
import com.msa.eshop.backend.common.dtos.OrderAddressDto
import com.msa.eshop.backend.common.dtos.PaymentTermDto
import com.msa.eshop.backend.service.CartService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/cart")
class CartController(
    private val cartService: CartService
) {

    @GetMapping("/addresses")
    fun addresses(): BaseResponse<List<OrderAddressDto>> =
        BaseResponse(
            data = cartService.currentCustomerAddresses()
        )

    @GetMapping("/payment-terms")
    fun paymentTerms(): BaseResponse<List<PaymentTermDto>> =
        BaseResponse(
            data = cartService.paymentTerms()
        )

    @PostMapping("/simulate")
    fun simulate(
        @Valid @RequestBody request: CartSimulateRequest
    ): BaseResponse<CartSimulateResponse> =
        BaseResponse(
            data = cartService.simulate(request)
        )

    @PostMapping("/checkout")
    fun checkout(
        @Valid @RequestBody request: CartCheckoutRequest
    ): BaseResponse<CartCheckoutResponse> =
        BaseResponse(
            data = cartService.checkout(request)
        )

    @GetMapping("/history")
    fun history(
        @RequestParam(required = false) fromDate: String?,
        @RequestParam(required = false) toDate: String?
    ): BaseResponse<List<CartHistoryDto>> =
        BaseResponse(
            data = cartService.history(
                fromDate = fromDate,
                toDate = toDate
            )
        )

    @GetMapping("/{cartCode}")
    fun details(
        @PathVariable cartCode: Int
    ): BaseResponse<CartDetailsDto> =
        BaseResponse(
            data = cartService.details(cartCode)
        )

    @PostMapping("/checkout")
    fun checkout(
        @RequestHeader("Idempotency-Key", required = false) idempotencyKey: String?,
        @Valid @RequestBody request: CartCheckoutRequest
    ): BaseResponse<CartCheckoutResponse> =
        BaseResponse(
            data = cartService.checkout(
                request = request,
                idempotencyKey = idempotencyKey
            )
        )
}