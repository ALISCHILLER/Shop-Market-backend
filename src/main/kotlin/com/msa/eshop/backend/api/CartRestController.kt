package com.msa.eshop.backend.api

import com.msa.eshop.backend.common.BaseResponse
import com.msa.eshop.backend.common.dtos.CartCheckoutRequest
import com.msa.eshop.backend.common.dtos.CartSimulateRequest
import com.msa.eshop.backend.common.dtos.CartSimulateResponse
import com.msa.eshop.backend.common.dtos.InsertCartModelRequest
import com.msa.eshop.backend.common.dtos.PaymentTermDto
import com.msa.eshop.backend.common.dtos.ReportCartDetailsDto
import com.msa.eshop.backend.common.dtos.ReportHistoryCustomerDto
import com.msa.eshop.backend.common.dtos.ReportHistoryCustomerModelRequest
import com.msa.eshop.backend.service.CartService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/cart")
class CartRestController(
    private val cartService: CartService
) {

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
            data = cartService.simulateModern(request)
        )

    @PostMapping("/checkout")
    fun checkout(
        @Valid @RequestBody request: CartCheckoutRequest
    ): BaseResponse<Boolean> {
        val legacyRequest = request.items.map { item ->
            InsertCartModelRequest(
                customerAddressId = request.customerAddressId.toString(),
                paymentTermId = request.paymentTermId.toString(),
                productCode = item.productCode,
                quantity = item.quantity
            )
        }

        return BaseResponse(
            data = cartService.insertCart(legacyRequest)
        )
    }

    @GetMapping("/history")
    fun history(
        @RequestParam(required = false) customerId: UUID?,
        @RequestParam(required = false) fromDate: String?,
        @RequestParam(required = false) toDate: String?
    ): BaseResponse<List<ReportHistoryCustomerDto>> =
        BaseResponse(
            data = cartService.history(
                ReportHistoryCustomerModelRequest(
                    customerId = customerId?.toString().orEmpty(),
                    fromDate = fromDate.orEmpty(),
                    endDate = toDate.orEmpty()
                )
            )
        )

    @GetMapping("/{cartCode}")
    fun details(
        @PathVariable cartCode: Int
    ): BaseResponse<List<ReportCartDetailsDto>> =
        BaseResponse(
            data = cartService.details(cartCode)
        )
}