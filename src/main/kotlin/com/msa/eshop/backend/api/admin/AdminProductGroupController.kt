package com.msa.eshop.backend.api.admin

import com.msa.eshop.backend.common.BaseResponse
import com.msa.eshop.backend.common.ProductGroupDto
import com.msa.eshop.backend.common.UpsertProductGroupRequest
import com.msa.eshop.backend.service.admin.AdminProductGroupService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/product-groups")
class AdminProductGroupController(
    private val productGroupService: AdminProductGroupService
) {
    @GetMapping
    fun productGroups(): BaseResponse<List<ProductGroupDto>> =
        BaseResponse(productGroupService.findAll())

    @PostMapping
    fun upsert(
        @Valid @RequestBody request: UpsertProductGroupRequest
    ): BaseResponse<ProductGroupDto> =
        BaseResponse(productGroupService.upsert(request))

    @DeleteMapping("/{code}")
    fun delete(
        @PathVariable code: Int
    ): BaseResponse<Boolean> {
        productGroupService.delete(code)
        return BaseResponse(true)
    }
}