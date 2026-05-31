package com.msa.eshop.backend.service.admin


import com.msa.eshop.backend.common.dtos.DashboardDto
import com.msa.eshop.backend.domain.repository.CartRepository
import com.msa.eshop.backend.domain.repository.CustomerRepository
import com.msa.eshop.backend.domain.repository.ProductCategoryRepository
import com.msa.eshop.backend.domain.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminDashboardService(
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val productCategoryRepository: ProductCategoryRepository,
    private val cartRepository: CartRepository
) {
    @Transactional(readOnly = true)
    fun dashboard(): DashboardDto =
        DashboardDto(
            customers = customerRepository.count(),
            products = productRepository.count(),
            categories = productCategoryRepository.count(),
            carts = cartRepository.count(),
            revenue = cartRepository.revenue()
        )
}