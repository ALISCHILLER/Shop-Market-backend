package com.msa.eshop.backend.service.admin

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.cleanOrNull
import com.msa.eshop.backend.common.dtos.ProductGroupDto
import com.msa.eshop.backend.common.dtos.UpsertProductGroupRequest
import com.msa.eshop.backend.domain.entity.ProductCategory
import com.msa.eshop.backend.domain.repository.ProductCategoryRepository
import com.msa.eshop.backend.domain.repository.ProductRepository
import com.msa.eshop.backend.service.audit.AuditLogService
import com.msa.eshop.backend.service.toDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminProductGroupService(
    private val categoryRepository: ProductCategoryRepository,
    private val productRepository: ProductRepository,
    private val auditLogService: AuditLogService
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

        val existingCategory = categoryRepository.findById(request.productCategoryCode).orElse(null)
        val oldSnapshot = existingCategory?.let { categorySnapshot(it) }

        val category = existingCategory
            ?: ProductCategory(productCategoryCode = request.productCategoryCode)

        category.productCategoryName = name
        category.productCategoryImage = request.productCategoryImage.cleanOrNull()
        category.productCategoryImageUnselect = request.productCategoryImageUnselect.cleanOrNull()

        val saved = categoryRepository.save(category)

        auditLogService.record(
            action = if (oldSnapshot == null) "PRODUCT_GROUP_CREATED" else "PRODUCT_GROUP_UPDATED",
            entityType = ENTITY_TYPE_PRODUCT_GROUP,
            entityId = saved.productCategoryCode.toString(),
            oldValue = oldSnapshot,
            newValue = categorySnapshot(saved),
            description = if (oldSnapshot == null) "Product group created by admin" else "Product group updated by admin"
        )

        return saved.toDto()
    }

    @Transactional
    fun delete(code: Int) {
        if (code <= 0) throw BadRequestException("کد دسته‌بندی معتبر نیست")

        val category = categoryRepository.findById(code)
            .orElseThrow { NotFoundException("دسته‌بندی پیدا نشد") }

        if (productRepository.countByProductGroupCode(code) > 0) {
            throw BadRequestException("این دسته‌بندی دارای کالا است و قابل حذف نیست")
        }

        val oldSnapshot = categorySnapshot(category)

        auditLogService.record(
            action = "PRODUCT_GROUP_DELETED",
            entityType = ENTITY_TYPE_PRODUCT_GROUP,
            entityId = code.toString(),
            oldValue = oldSnapshot,
            newValue = null,
            description = "Product group deleted by admin"
        )

        categoryRepository.delete(category)
    }

    private fun categorySnapshot(category: ProductCategory): Map<String, Any?> =
        auditLogService.snapshotOf(
            "productCategoryCode" to category.productCategoryCode,
            "productCategoryName" to category.productCategoryName,
            "productCategoryImage" to category.productCategoryImage,
            "productCategoryImageUnselect" to category.productCategoryImageUnselect
        )

    private companion object {
        const val ENTITY_TYPE_PRODUCT_GROUP = "ProductCategory"
    }
}