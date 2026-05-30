package com.msa.eshop.backend.service

import com.msa.eshop.backend.common.dtos.BannerDto
import com.msa.eshop.backend.common.dtos.DiscountResultDto
import com.msa.eshop.backend.common.dtos.ProductDto
import com.msa.eshop.backend.common.dtos.ProductGroupDto
import com.msa.eshop.backend.domain.BannerRepository
import com.msa.eshop.backend.domain.DiscountRepository
import com.msa.eshop.backend.domain.ProductCategoryRepository
import com.msa.eshop.backend.domain.ProductRepository
import com.msa.eshop.backend.service.catalog.ProductResolver
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CatalogService(
    private val productRepository: ProductRepository,
    private val productCategoryRepository: ProductCategoryRepository,
    private val discountRepository: DiscountRepository,
    private val bannerRepository: BannerRepository,
    private val productResolver: ProductResolver
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

        val product = productResolver.requireByIdOrCode(value)
        val productId = requireNotNull(product.id)

        return discountRepository.findByProductId(productId)
            .sortedBy { it.fromNumber }
            .map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun getProduct(productId: UUID): ProductDto =
        productResolver.requireById(productId).toDto()
}