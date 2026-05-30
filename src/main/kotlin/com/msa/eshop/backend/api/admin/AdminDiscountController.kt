package com.msa.eshop.backend.api.admin

import com.msa.eshop.backend.common.dtos.BaseResponse
import com.msa.eshop.backend.common.dtos.DiscountResultDto
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
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/discounts")
class AdminDiscountController(
    private val discountService: AdminDiscountService
) {
    @GetMapping
    fun discounts(): BaseResponse<List<DiscountResultDto>> =
        BaseResponse(discountService.findAll())

    @PostMapping
    fun create(
        @Valid @RequestBody request: UpsertDiscountRequest
    ): BaseResponse<DiscountResultDto> =
        BaseResponse(discountService.create(request))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpsertDiscountRequest
    ): BaseResponse<DiscountResultDto> =
        BaseResponse(discountService.update(id, request))

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID
    ): BaseResponse<Boolean> {
        discountService.delete(id)
        return BaseResponse(true)
    }
}