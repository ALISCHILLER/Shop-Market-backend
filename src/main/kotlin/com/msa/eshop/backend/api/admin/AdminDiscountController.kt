package com.msa.eshop.backend.api.admin

import com.msa.eshop.backend.common.BaseResponse
import com.msa.eshop.backend.common.dtos.DiscountResultDto
import com.msa.eshop.backend.common.dtos.PageResponseDto
import com.msa.eshop.backend.common.dtos.UpsertDiscountRequest
import com.msa.eshop.backend.service.admin.AdminDiscountService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/discounts")
class AdminDiscountController(
    private val discountService: AdminDiscountService
) {

    @GetMapping
    fun discounts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) productIdOrCode: String?,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "fromNumber") sortBy: String,
        @RequestParam(defaultValue = "ASC") direction: String
    ): BaseResponse<PageResponseDto<DiscountResultDto>> =
        BaseResponse(
            data = discountService.search(
                page = page,
                size = size,
                productIdOrCode = productIdOrCode,
                search = search,
                sortBy = sortBy,
                direction = direction
            )
        )

    @PostMapping
    fun create(
        @Valid @RequestBody request: UpsertDiscountRequest
    ): BaseResponse<DiscountResultDto> =
        BaseResponse(
            data = discountService.create(request)
        )

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpsertDiscountRequest
    ): BaseResponse<DiscountResultDto> =
        BaseResponse(
            data = discountService.update(id, request)
        )

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID
    ): BaseResponse<Boolean> {
        discountService.delete(id)
        return BaseResponse(data = true)
    }
}