package com.msa.eshop.backend.api

import com.msa.eshop.backend.common.DiscountResponse
import com.msa.eshop.backend.common.ProductGroupResponse
import com.msa.eshop.backend.common.ProductResponse
import com.msa.eshop.backend.service.CatalogService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/Product")
class ProductController(
    private val catalogService: CatalogService
) {
    @GetMapping("/GetListKala")
    fun products(): ProductResponse = ProductResponse(catalogService.products())

    @GetMapping("/GetProductCategory")
    fun productGroups(): ProductGroupResponse = ProductGroupResponse(catalogService.productGroups())

    @GetMapping("/GetListDiscounts")
    fun discounts(@RequestParam("ProductID") productId: String): DiscountResponse =
        DiscountResponse(catalogService.discounts(productId))
}
