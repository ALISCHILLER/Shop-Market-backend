package com.msa.eshop.backend.service

import com.msa.eshop.backend.common.BannerDto
import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.DiscountResultDto
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.ProductDto
import com.msa.eshop.backend.common.ProductGroupDto
import com.msa.eshop.backend.domain.BannerRepository
import com.msa.eshop.backend.domain.DiscountRepository
import com.msa.eshop.backend.domain.ProductCategoryRepository
import com.msa.eshop.backend.domain.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CatalogService(
    private val productRepository: ProductRepository,
    private val productCategoryRepository: ProductCategoryRepository,
    private val discountRepository: DiscountRepository,
    private val bannerRepository: BannerRepository
) {
    @Transactional(readOnly = true)
    fun products(): List<ProductDto> =
        productRepository.findAllByOrderByProductNameAsc()
            .map { it.toDto() }

    @Transactional(readOnly = true)
    fun productGroups(): List<ProductGroupDto> =
        productCategoryRepository.findAllByOrderByProductCategoryCodeAsc()
            .map { it.toDto() }

    @Transactional(readOnly = true)
    fun banners(): List<BannerDto> =
        bannerRepository.findAllByOrderByBannerNameAsc()
            .map { it.toDto() }

    @Transactional(readOnly = true)
    fun discounts(productIdOrCode: String): List<DiscountResultDto> {
        val value = productIdOrCode.trim()

        if (value.isBlank()) {
            return discountRepository.findAllByOrderByFromNumberAsc()
                .map { it.toDto() }
        }

        val byUuid = runCatching { UUID.fromString(value) }.getOrNull()
        if (byUuid != null) {
            return discountRepository.findByProductId(byUuid)
                .sortedBy { it.fromNumber }
                .map { it.toDto() }
        }

        val byCode = value.toIntOrNull()
        if (byCode != null) {
            return discountRepository.findByProductProductCode(byCode)
                .sortedBy { it.fromNumber }
                .map { it.toDto() }
        }

        throw BadRequestException("شناسه کالا معتبر نیست")
    }

    @Transactional(readOnly = true)
    fun getProduct(productId: UUID): ProductDto =
        productRepository.findById(productId)
            .orElseThrow { NotFoundException("کالا پیدا نشد") }
            .toDto()
}