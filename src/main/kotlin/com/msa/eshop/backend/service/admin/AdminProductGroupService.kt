package com.msa.eshop.backend.service.admin

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.dtos.ProductGroupDto
import com.msa.eshop.backend.common.dtos.UpsertProductGroupRequest
import com.msa.eshop.backend.common.cleanOrNull
import com.msa.eshop.backend.domain.ProductCategory
import com.msa.eshop.backend.domain.ProductCategoryRepository
import com.msa.eshop.backend.domain.ProductRepository
import com.msa.eshop.backend.service.toDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminProductGroupService(
    private val categoryRepository: ProductCategoryRepository,
    private val productRepository: ProductRepository
) {
    @Transactional(readOnly = true)
    fun findAll(): List<ProductGroupDto> =
        categoryRepository.findAllByOrderByProductCategoryCodeAsc()
            .map { it.toDto() }

    @Transactional
    fun upsert(request: UpsertProductGroupRequest): ProductGroupDto {
        if (request.productCategoryCode <= 0) {
            throw BadRequestException("کد دسته‌بندی معتبر نیست")
        }

        val name = request.productCategoryName
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: throw BadRequestException("نام دسته‌بندی الزامی است")

        val category = categoryRepository.findById(request.productCategoryCode)
            .orElse(ProductCategory(productCategoryCode = request.productCategoryCode))

        category.productCategoryName = name
        category.productCategoryImage = request.productCategoryImage.cleanOrNull()
        category.productCategoryImageUnselect = request.productCategoryImageUnselect.cleanOrNull()

        return categoryRepository.save(category).toDto()
    }

    @Transactional
    fun delete(code: Int) {
        if (code <= 0) throw BadRequestException("کد دسته‌بندی معتبر نیست")

        if (!categoryRepository.existsById(code)) {
            throw NotFoundException("دسته‌بندی پیدا نشد")
        }

        if (productRepository.countByProductGroupCode(code) > 0) {
            throw BadRequestException("این دسته‌بندی دارای کالا است و قابل حذف نیست")
        }

        categoryRepository.deleteById(code)
    }
}