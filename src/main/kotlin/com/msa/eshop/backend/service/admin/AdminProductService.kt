package com.msa.eshop.backend.service.admin

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.dtos.PageResponseDto
import com.msa.eshop.backend.common.dtos.ProductDto
import com.msa.eshop.backend.common.dtos.UpsertProductRequest
import com.msa.eshop.backend.common.cleanOrNull
import com.msa.eshop.backend.common.createPageable
import com.msa.eshop.backend.common.toPageResponse
import com.msa.eshop.backend.domain.CartItemRepository
import com.msa.eshop.backend.domain.Product
import com.msa.eshop.backend.domain.ProductCategoryRepository
import com.msa.eshop.backend.domain.ProductRepository
import com.msa.eshop.backend.service.toDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: ProductCategoryRepository,
    private val cartItemRepository: CartItemRepository
) {
    @Transactional(readOnly = true)
    fun findAll(): List<ProductDto> =
        productRepository.findAllByOrderByProductNameAsc()
            .map { it.toDto() }



    @Transactional
    fun create(request: UpsertProductRequest): ProductDto {
        validateRequest(request)
        requireCategory(request.productGroupCode)

        if (productRepository.existsByProductCode(request.productCode)) {
            throw BadRequestException("کد کالا قبلاً ثبت شده است")
        }

        val product = Product()
        product.applyRequest(request)

        return productRepository.save(product).toDto()
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

        product.applyRequest(request)

        return productRepository.save(product).toDto()
    }

    @Transactional
    fun delete(id: UUID) {
        val product = productRepository.findById(id)
            .orElseThrow { NotFoundException("کالا پیدا نشد") }

        if (cartItemRepository.countByProductId(id) > 0) {
            throw BadRequestException("این کالا در سفارش استفاده شده و قابل حذف نیست")
        }

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
    }

    private fun requireCategory(code: Int) {
        if (!categoryRepository.existsById(code)) {
            throw BadRequestException("دسته‌بندی کالا پیدا نشد")
        }
    }

    @Transactional(readOnly = true)
    fun search(
        page: Int,
        size: Int,
        search: String?,
        productGroupCode: Int?,
        isDiscounts: Boolean?,
        isTax: Boolean?,
        sortBy: String = "productName",
        direction: String = "ASC"
    ): PageResponseDto<ProductDto> {
        if (productGroupCode != null && productGroupCode <= 0) {
            throw BadRequestException("کد گروه کالا معتبر نیست")
        }

        val pageable = createPageable(
            page = page,
            size = size,
            sortBy = sortBy,
            direction = direction,
            allowedSorts = setOf("productName", "productCode", "price")
        )

        return productRepository.searchAdminProducts(
            search = search.cleanOrNull(),
            productGroupCode = productGroupCode,
            isDiscounts = isDiscounts,
            isTax = isTax,
            pageable = pageable
        ).toPageResponse { it.toDto() }
    }
}