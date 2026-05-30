package com.msa.eshop.backend.api.admin

import com.msa.eshop.backend.common.BaseResponse
import com.msa.eshop.backend.common.PageResponseDto
import com.msa.eshop.backend.common.ProductDto
import com.msa.eshop.backend.common.UpsertProductRequest
import com.msa.eshop.backend.service.admin.AdminProductService
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
@RequestMapping("/api/v1/admin/products")
class AdminProductController(
    private val productService: AdminProductService
) {
    @GetMapping
    fun products(): BaseResponse<List<ProductDto>> =
        BaseResponse(productService.findAll())

    @GetMapping("/page")
    fun productsPage(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) productGroupCode: Int?,
        @RequestParam(required = false) isDiscounts: Boolean?,
        @RequestParam(required = false) isTax: Boolean?
    ): BaseResponse<PageResponseDto<ProductDto>> =
        BaseResponse(
            productService.search(
                page = page,
                size = size,
                search = search,
                productGroupCode = productGroupCode,
                isDiscounts = isDiscounts,
                isTax = isTax
            )
        )

    @PostMapping
    fun create(
        @Valid @RequestBody request: UpsertProductRequest
    ): BaseResponse<ProductDto> =
        BaseResponse(productService.create(request))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpsertProductRequest
    ): BaseResponse<ProductDto> =
        BaseResponse(productService.update(id, request))

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID
    ): BaseResponse<Boolean> {
        productService.delete(id)
        return BaseResponse(true)
    }
}