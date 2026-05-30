package com.msa.eshop.backend.api.admin

import com.msa.eshop.backend.common.BaseResponse
import com.msa.eshop.backend.common.PaymentTermDto
import com.msa.eshop.backend.common.UpsertPaymentTermRequest
import com.msa.eshop.backend.service.admin.AdminPaymentTermService
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
@RequestMapping("/api/v1/admin/payment-terms")
class AdminPaymentTermController(
    private val paymentTermService: AdminPaymentTermService
) {
    @GetMapping
    fun paymentTerms(): BaseResponse<List<PaymentTermDto>> =
        BaseResponse(paymentTermService.findAll())

    @PostMapping
    fun create(
        @Valid @RequestBody request: UpsertPaymentTermRequest
    ): BaseResponse<PaymentTermDto> =
        BaseResponse(paymentTermService.create(request))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpsertPaymentTermRequest
    ): BaseResponse<PaymentTermDto> =
        BaseResponse(paymentTermService.update(id, request))

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID
    ): BaseResponse<Boolean> {
        paymentTermService.delete(id)
        return BaseResponse(true)
    }
}