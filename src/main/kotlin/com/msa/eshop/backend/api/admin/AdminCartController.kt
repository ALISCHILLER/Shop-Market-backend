package com.msa.eshop.backend.api.admin

import com.msa.eshop.backend.common.dtos.AdminCartSummaryDto
import com.msa.eshop.backend.common.BaseResponse
import com.msa.eshop.backend.common.dtos.PageResponseDto
import com.msa.eshop.backend.common.dtos.ReportCartDetailsDto
import com.msa.eshop.backend.common.dtos.UpdateCartStatusRequest
import com.msa.eshop.backend.service.admin.AdminCartService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/carts")
class AdminCartController(
    private val cartService: AdminCartService
) {
    @GetMapping
    fun carts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) cartCode: Int?,
        @RequestParam(required = false) customerSearch: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) fromDate: String?,
        @RequestParam(required = false) toDate: String?,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") direction: String
    ): BaseResponse<PageResponseDto<AdminCartSummaryDto>> =
        BaseResponse(
            cartService.findAll(
                page = page,
                size = size,
                cartCode = cartCode,
                customerSearch = customerSearch,
                status = status,
                fromDate = fromDate,
                toDate = toDate,
                sortBy = sortBy,
                direction = direction
            )
        )

    @GetMapping("/{cartCode}")
    fun details(
        @PathVariable cartCode: Int
    ): BaseResponse<List<ReportCartDetailsDto>> =
        BaseResponse(cartService.details(cartCode))

    @PutMapping("/{cartCode}/status")
    fun updateStatus(
        @PathVariable cartCode: Int,
        @Valid @RequestBody request: UpdateCartStatusRequest
    ): BaseResponse<AdminCartSummaryDto> =
        BaseResponse(cartService.updateStatus(cartCode, request))
}