package com.msa.eshop.backend.api

import com.msa.eshop.backend.common.dtos.BannerResponse
import com.msa.eshop.backend.service.CatalogService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/Banner")
class BannerController(
    private val catalogService: CatalogService
) {
    @GetMapping("/GetBanner")
    fun banners(): BannerResponse =
        BannerResponse(catalogService.banners())
}