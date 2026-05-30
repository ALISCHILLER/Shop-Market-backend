package com.msa.eshop.backend.service.admin


import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.cleanRequired
import com.msa.eshop.backend.common.dtos.BannerDto
import com.msa.eshop.backend.common.dtos.UpsertBannerRequest
import com.msa.eshop.backend.domain.Banner
import com.msa.eshop.backend.domain.BannerRepository
import com.msa.eshop.backend.service.toDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminBannerService(
    private val bannerRepository: BannerRepository
) {
    @Transactional(readOnly = true)
    fun findAll(): List<BannerDto> =
        bannerRepository.findAllByOrderByBannerNameAsc()
            .map { it.toDto() }

    @Transactional
    fun create(request: UpsertBannerRequest): BannerDto {
        val banner = Banner(
            bannerImage = request.bannerImage.cleanRequired("تصویر بنر الزامی است"),
            bannerName = request.bannerName.cleanRequired("نام بنر الزامی است")
        )

        return bannerRepository.save(banner).toDto()
    }

    @Transactional
    fun update(id: UUID, request: UpsertBannerRequest): BannerDto {
        val banner = bannerRepository.findById(id)
            .orElseThrow { NotFoundException("بنر پیدا نشد") }

        banner.bannerImage = request.bannerImage.cleanRequired("تصویر بنر الزامی است")
        banner.bannerName = request.bannerName.cleanRequired("نام بنر الزامی است")

        return bannerRepository.save(banner).toDto()
    }

    @Transactional
    fun delete(id: UUID) {
        val banner = bannerRepository.findById(id)
            .orElseThrow { NotFoundException("بنر پیدا نشد") }

        bannerRepository.delete(banner)
    }
}