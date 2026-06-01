package com.msa.eshop.backend.api

import com.msa.eshop.backend.common.BaseResponse
import com.msa.eshop.backend.common.dtos.BannerDto
import com.msa.eshop.backend.common.dtos.DiscountResultDto
import com.msa.eshop.backend.common.dtos.PageResponseDto
import com.msa.eshop.backend.common.dtos.ProductDto
import com.msa.eshop.backend.common.dtos.ProductGroupDto
import com.msa.eshop.backend.service.CatalogService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class CatalogRestController(
    private val catalogService: CatalogService
) {

    @GetMapping("/products")
    fun products(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) categoryCode: Int?,
        @RequestParam(required = false) hasDiscount: Boolean?,
        @RequestParam(defaultValue = "productName") sortBy: String,
        @RequestParam(defaultValue = "ASC") direction: String
    ): BaseResponse<PageResponseDto<ProductDto>> =
        BaseResponse(
            data = catalogService.searchProducts(
                page = page,
                size = size,
                search = search,
                categoryCode = categoryCode,
                hasDiscount = hasDiscount,
                sortBy = sortBy,
                direction = direction
            )
        )

    @GetMapping("/products/{id}")
    fun product(
        @PathVariable id: UUID
    ): BaseResponse<ProductDto> =
        BaseResponse(
            data = catalogService.getProduct(id)
        )

    @GetMapping("/products/{id}/discounts")
    fun productDiscounts(
        @PathVariable id: UUID
    ): BaseResponse<List<DiscountResultDto>> =
        BaseResponse(
            data = catalogService.discounts(id.toString())
        )

    @GetMapping("/product-categories")
    fun productCategories(): BaseResponse<List<ProductGroupDto>> =
        BaseResponse(
            data = catalogService.productGroups()
        )

    @GetMapping("/banners")
    fun banners(): BaseResponse<List<BannerDto>> =
        BaseResponse(
            data = catalogService.banners()
        )
}