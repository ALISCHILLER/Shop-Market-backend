package com.msa.eshop.backend.api.admin

import com.msa.eshop.backend.common.dtos.BaseResponse
import com.msa.eshop.backend.common.dtos.OrderAddressDto
import com.msa.eshop.backend.common.dtos.UpsertAddressRequest
import com.msa.eshop.backend.service.admin.AdminAddressService
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
@RequestMapping("/api/v1/admin/addresses")
class AdminAddressController(
    private val addressService: AdminAddressService
) {
    @GetMapping
    fun addresses(
        @RequestParam(required = false) customerId: UUID?
    ): BaseResponse<List<OrderAddressDto>> =
        BaseResponse(addressService.findAll(customerId))

    @PostMapping
    fun create(
        @Valid @RequestBody request: UpsertAddressRequest
    ): BaseResponse<OrderAddressDto> =
        BaseResponse(addressService.create(request))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpsertAddressRequest
    ): BaseResponse<OrderAddressDto> =
        BaseResponse(addressService.update(id, request))

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID
    ): BaseResponse<Boolean> {
        addressService.delete(id)
        return BaseResponse(true)
    }
}