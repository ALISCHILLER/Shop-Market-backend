package com.msa.eshop.backend.service.catalog

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.domain.Product
import com.msa.eshop.backend.domain.ProductRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ProductResolver(
    private val productRepository: ProductRepository
) {
    fun requireById(id: UUID): Product =
        productRepository.findById(id)
            .orElseThrow { NotFoundException("کالا پیدا نشد") }

    fun requireByCode(productCode: Int): Product {
        if (productCode <= 0) {
            throw BadRequestException("کد کالا معتبر نیست")
        }

        return productRepository.findByProductCode(productCode)
            ?: throw NotFoundException("کالا با کد $productCode پیدا نشد")
    }

    fun requireByIdOrCode(value: String): Product {
        val normalized = value.trim()

        if (normalized.isBlank()) {
            throw BadRequestException("شناسه کالا الزامی است")
        }

        val asUuid = runCatching { UUID.fromString(normalized) }.getOrNull()
        if (asUuid != null) {
            return requireById(asUuid)
        }

        val asCode = normalized.toIntOrNull()
            ?: throw BadRequestException("شناسه کالا معتبر نیست")

        return requireByCode(asCode)
    }

    fun requireByCodes(productCodes: Collection<Int>): Map<Int, Product> {
        val uniqueCodes = productCodes
            .asSequence()
            .map {
                if (it <= 0) throw BadRequestException("کد کالا معتبر نیست")
                it
            }
            .toSet()

        if (uniqueCodes.isEmpty()) return emptyMap()

        val products = productRepository.findByProductCodeIn(uniqueCodes)
            .associateBy { it.productCode }

        val missingCodes = uniqueCodes - products.keys
        if (missingCodes.isNotEmpty()) {
            throw NotFoundException("کالا با کد ${missingCodes.first()} پیدا نشد")
        }

        return products
    }
}