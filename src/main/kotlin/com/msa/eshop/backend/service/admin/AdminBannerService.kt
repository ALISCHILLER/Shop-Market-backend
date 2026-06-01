package com.msa.eshop.backend.service.admin

import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.cleanRequired
import com.msa.eshop.backend.common.dtos.BannerDto
import com.msa.eshop.backend.common.dtos.UpsertBannerRequest
import com.msa.eshop.backend.domain.entity.Banner
import com.msa.eshop.backend.domain.repository.BannerRepository
import com.msa.eshop.backend.service.audit.AuditLogService
import com.msa.eshop.backend.service.toDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminBannerService(
    private val bannerRepository: BannerRepository,
    private val auditLogService: AuditLogService
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

        val saved = bannerRepository.save(banner)

        auditLogService.record(
            action = "BANNER_CREATED",
            entityType = ENTITY_TYPE_BANNER,
            entityId = saved.id?.toString(),
            oldValue = null,
            newValue = bannerSnapshot(saved),
            description = "Banner created by admin"
        )

        return saved.toDto()
    }

    @Transactional
    fun update(id: UUID, request: UpsertBannerRequest): BannerDto {
        val banner = bannerRepository.findById(id)
            .orElseThrow { NotFoundException("بنر پیدا نشد") }

        val oldSnapshot = bannerSnapshot(banner)

        banner.bannerImage = request.bannerImage.cleanRequired("تصویر بنر الزامی است")
        banner.bannerName = request.bannerName.cleanRequired("نام بنر الزامی است")

        val saved = bannerRepository.save(banner)

        auditLogService.record(
            action = "BANNER_UPDATED",
            entityType = ENTITY_TYPE_BANNER,
            entityId = saved.id?.toString(),
            oldValue = oldSnapshot,
            newValue = bannerSnapshot(saved),
            description = "Banner updated by admin"
        )

        return saved.toDto()
    }

    @Transactional
    fun delete(id: UUID) {
        val banner = bannerRepository.findById(id)
            .orElseThrow { NotFoundException("بنر پیدا نشد") }

        val oldSnapshot = bannerSnapshot(banner)

        auditLogService.record(
            action = "BANNER_DELETED",
            entityType = ENTITY_TYPE_BANNER,
            entityId = banner.id?.toString(),
            oldValue = oldSnapshot,
            newValue = null,
            description = "Banner deleted by admin"
        )

        bannerRepository.delete(banner)
    }

    private fun bannerSnapshot(banner: Banner): Map<String, Any?> =
        auditLogService.snapshotOf(
            "id" to banner.id,
            "bannerName" to banner.bannerName,
            "bannerImage" to banner.bannerImage
        )

    private companion object {
        const val ENTITY_TYPE_BANNER = "Banner"
    }
}