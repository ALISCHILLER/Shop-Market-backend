package com.msa.eshop.backend.service.admin

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.cleanOrNull
import com.msa.eshop.backend.common.createPageable
import com.msa.eshop.backend.common.dtos.PageResponseDto
import com.msa.eshop.backend.common.dtos.ProductDto
import com.msa.eshop.backend.common.dtos.UpsertProductRequest
import com.msa.eshop.backend.common.toPageResponse
import com.msa.eshop.backend.domain.entity.Product
import com.msa.eshop.backend.domain.repository.CartItemRepository
import com.msa.eshop.backend.domain.repository.ProductCategoryRepository
import com.msa.eshop.backend.domain.repository.ProductRepository
import com.msa.eshop.backend.service.audit.AuditLogService
import com.msa.eshop.backend.service.toDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: ProductCategoryRepository,
    private val cartItemRepository: CartItemRepository,
    private val auditLogService: AuditLogService
) {

    @Transactional(readOnly = true)
    fun findAll(): List<ProductDto> =
        productRepository.findAllByOrderByProductNameAsc()
            .map { it.toDto() }

    @Transactional(readOnly = true)
    fun search(
        page: Int,
        size: Int,
        search: String?,
        productGroupCode: Int?,
        isDiscounts: Boolean?,
        isTax: Boolean?,
        sortBy: String = DEFAULT_SORT_BY,
        direction: String = DEFAULT_SORT_DIRECTION
    ): PageResponseDto<ProductDto> {
        if (productGroupCode != null && productGroupCode <= 0) {
            throw BadRequestException("کد گروه کالا معتبر نیست")
        }

        val pageable = createPageable(
            page = page,
            size = size,
            sortBy = sortBy,
            direction = direction,
            allowedSorts = ALLOWED_SORTS
        )

        return productRepository.searchAdminProducts(
            search = search.cleanOrNull(),
            productGroupCode = productGroupCode,
            isDiscounts = isDiscounts,
            isTax = isTax,
            pageable = pageable
        ).toPageResponse { it.toDto() }
    }

    @Transactional
    fun create(request: UpsertProductRequest): ProductDto {
        validateRequest(request)
        requireCategory(request.productGroupCode)

        if (productRepository.existsByProductCode(request.productCode)) {
            throw BadRequestException("کد کالا قبلاً ثبت شده است")
        }

        val product = Product().apply {
            applyRequest(request)
        }

        val savedProduct = productRepository.save(product)

        auditLogService.record(
            action = "PRODUCT_CREATED",
            entityType = ENTITY_TYPE_PRODUCT,
            entityId = savedProduct.id?.toString(),
            oldValue = null,
            newValue = productSnapshot(savedProduct),
            description = "Product created by admin"
        )
        product.setStockOnHand(request.stockOnHand)
        return savedProduct.toDto()
    }

    @Transactional
    fun update(id: UUID, request: UpsertProductRequest): ProductDto {
        validateRequest(request)
        requireCategory(request.productGroupCode)

        if (productRepository.existsByProductCodeAndIdNot(request.productCode, id)) {
            throw BadRequestException("کد کالا قبلاً برای کالای دیگری ثبت شده است")
        }

        val product = productRepository.findById(id)
            .orElseThrow { NotFoundException("کالا پیدا نشد") }

        val oldSnapshot = productSnapshot(product)

        product.applyRequest(request)

        val savedProduct = productRepository.save(product)

        auditLogService.record(
            action = "PRODUCT_UPDATED",
            entityType = ENTITY_TYPE_PRODUCT,
            entityId = savedProduct.id?.toString(),
            oldValue = oldSnapshot,
            newValue = productSnapshot(savedProduct),
            description = "Product updated by admin"
        )

        return savedProduct.toDto()
    }

    @Transactional
    fun delete(id: UUID) {
        val product = productRepository.findById(id)
            .orElseThrow { NotFoundException("کالا پیدا نشد") }

        if (cartItemRepository.countByProductId(id) > 0) {
            throw BadRequestException("این کالا در سفارش استفاده شده و قابل حذف نیست")
        }

        val oldSnapshot = productSnapshot(product)

        auditLogService.record(
            action = "PRODUCT_DELETED",
            entityType = ENTITY_TYPE_PRODUCT,
            entityId = product.id?.toString(),
            oldValue = oldSnapshot,
            newValue = null,
            description = "Product deleted by admin"
        )

        productRepository.delete(product)
    }

    private fun Product.applyRequest(request: UpsertProductRequest) {
        productName = request.productName.cleanOrNull()
        productCode = request.productCode
        fullNameKala1 = request.fullNameKala1.cleanOrNull()
        unit1 = request.unit1.cleanOrNull()
        unitid1 = request.unitid1.cleanOrNull()
        convertFactor1 = request.convertFactor1
        fullNameKala2 = request.fullNameKala2.cleanOrNull()
        unit2 = request.unit2.cleanOrNull()
        convertFactor2 = request.convertFactor2
        unitid2 = request.unitid2.cleanOrNull()
        productGroupCode = request.productGroupCode
        price = request.price
        isDiscounts = request.isDiscounts
        isTax = request.isTax
        productImage = request.productImage.cleanOrNull()
    }

    private fun validateRequest(request: UpsertProductRequest) {
        if (request.productCode <= 0) {
            throw BadRequestException("کد کالا معتبر نیست")
        }

        if (request.productGroupCode <= 0) {
            throw BadRequestException("کد گروه کالا معتبر نیست")
        }

        if (request.price < 0) {
            throw BadRequestException("قیمت کالا معتبر نیست")
        }

        if (request.convertFactor1 <= 0) {
            throw BadRequestException("ضریب تبدیل واحد اول معتبر نیست")
        }

        if (request.convertFactor2 <= 0) {
            throw BadRequestException("ضریب تبدیل واحد دوم معتبر نیست")
        }
        if (request.stockOnHand < 0) {
            throw BadRequestException("موجودی کالا معتبر نیست")
        }
    }

    private fun requireCategory(code: Int) {
        if (!categoryRepository.existsById(code)) {
            throw BadRequestException("دسته‌بندی کالا پیدا نشد")
        }
    }

    private fun productSnapshot(product: Product): Map<String, Any?> =
        auditLogService.snapshotOf(
            "id" to product.id,
            "productCode" to product.productCode,
            "productName" to product.productName,
            "fullNameKala1" to product.fullNameKala1,
            "unit1" to product.unit1,
            "unitid1" to product.unitid1,
            "convertFactor1" to product.convertFactor1,
            "fullNameKala2" to product.fullNameKala2,
            "unit2" to product.unit2,
            "unitid2" to product.unitid2,
            "convertFactor2" to product.convertFactor2,
            "productGroupCode" to product.productGroupCode,
            "price" to product.price,
            "isTax" to product.isTax,
            "isDiscounts" to product.isDiscounts,
            "productImage" to product.productImage,
            "stockOnHand" to product.stockOnHand,
            "reservedStock" to product.reservedStock,
            "availableStock" to product.availableStock()
        )

    private companion object {
        const val ENTITY_TYPE_PRODUCT = "Product"
        const val DEFAULT_SORT_BY = "productName"
        const val DEFAULT_SORT_DIRECTION = "ASC"

        val ALLOWED_SORTS = setOf(
            "productName",
            "productCode",
            "price"
        )
    }
}