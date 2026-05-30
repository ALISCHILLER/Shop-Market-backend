package com.msa.eshop.backend.api.admin

import com.msa.eshop.backend.common.dtos.BannerDto
import com.msa.eshop.backend.common.BaseResponse
import com.msa.eshop.backend.common.dtos.UpsertBannerRequest
import com.msa.eshop.backend.service.admin.AdminBannerService
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
@RequestMapping("/api/v1/admin/banners")
class AdminBannerController(
    private val bannerService: AdminBannerService
) {
    @GetMapping
    fun banners(): BaseResponse<List<BannerDto>> =
        BaseResponse(bannerService.findAll())

    @PostMapping
    fun create(
        @Valid @RequestBody request: UpsertBannerRequest
    ): BaseResponse<BannerDto> =
        BaseResponse(bannerService.create(request))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpsertBannerRequest
    ): BaseResponse<BannerDto> =
        BaseResponse(bannerService.update(id, request))

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID
    ): BaseResponse<Boolean> {
        bannerService.delete(id)
        return BaseResponse(true)
    }
}