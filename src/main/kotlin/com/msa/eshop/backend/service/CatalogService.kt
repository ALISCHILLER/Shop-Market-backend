package com.msa.eshop.backend.service

import com.msa.eshop.backend.common.cleanOrNull
import com.msa.eshop.backend.common.createPageable
import com.msa.eshop.backend.common.dtos.BannerDto
import com.msa.eshop.backend.common.dtos.DiscountResultDto
import com.msa.eshop.backend.common.dtos.PageResponseDto
import com.msa.eshop.backend.common.dtos.ProductDto
import com.msa.eshop.backend.common.dtos.ProductGroupDto
import com.msa.eshop.backend.common.toPageResponse
import com.msa.eshop.backend.domain.repository.BannerRepository
import com.msa.eshop.backend.domain.repository.DiscountRepository
import com.msa.eshop.backend.domain.repository.ProductCategoryRepository
import com.msa.eshop.backend.domain.repository.ProductRepository
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

    @Transactional(readOnly = true)
    fun searchProducts(
        page: Int,
        size: Int,
        search: String?,
        categoryCode: Int?,
        hasDiscount: Boolean?,
        sortBy: String,
        direction: String
    ): PageResponseDto<ProductDto> {
        val pageable = createPageable(
            page = page,
            size = size,
            sortBy = sortBy,
            direction = direction,
            allowedSorts = setOf("productName", "productCode", "price", "createdAt")
        )

        return productRepository.searchPublicProducts(
            search = search.cleanOrNull(),
            categoryCode = categoryCode,
            hasDiscount = hasDiscount,
            pageable = pageable
        ).toPageResponse { it.toDto() }
    }
}