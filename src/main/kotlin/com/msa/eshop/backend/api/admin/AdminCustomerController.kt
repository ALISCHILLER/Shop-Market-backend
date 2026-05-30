package com.msa.eshop.backend.api.admin

import com.msa.eshop.backend.common.BaseResponse
import com.msa.eshop.backend.common.dtos.PageResponseDto
import com.msa.eshop.backend.common.dtos.UpsertCustomerRequest
import com.msa.eshop.backend.common.dtos.UserDto
import com.msa.eshop.backend.service.admin.AdminCustomerService
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
@RequestMapping("/api/v1/admin/customers")
class AdminCustomerController(
    private val customerService: AdminCustomerService
) {
    @GetMapping
    fun customers(): BaseResponse<List<UserDto>> =
        BaseResponse(customerService.findAll())

    @GetMapping("/page")
    fun customersPage(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) role: String?,
        @RequestParam(required = false) enabled: Boolean?,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") direction: String
    ): BaseResponse<PageResponseDto<UserDto>> =
        BaseResponse(
            customerService.search(
                page = page,
                size = size,
                search = search,
                role = role,
                enabled = enabled,
                sortBy = sortBy,
                direction = direction
            )
        )

    @PostMapping
    fun create(
        @Valid @RequestBody request: UpsertCustomerRequest
    ): BaseResponse<UserDto> =
        BaseResponse(customerService.create(request))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpsertCustomerRequest
    ): BaseResponse<UserDto> =
        BaseResponse(customerService.update(id, request))

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID
    ): BaseResponse<Boolean> {
        customerService.delete(id)
        return BaseResponse(true)
    }
}