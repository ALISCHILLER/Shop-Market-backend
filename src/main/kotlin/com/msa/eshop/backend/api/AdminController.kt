package com.msa.eshop.backend.api

import com.msa.eshop.backend.common.BannerDto
import com.msa.eshop.backend.common.BaseResponse
import com.msa.eshop.backend.common.DashboardDto
import com.msa.eshop.backend.common.DiscountResultDto
import com.msa.eshop.backend.common.OrderAddressDto
import com.msa.eshop.backend.common.PaymentTermDto
import com.msa.eshop.backend.common.ProductDto
import com.msa.eshop.backend.common.ProductGroupDto
import com.msa.eshop.backend.common.UpsertAddressRequest
import com.msa.eshop.backend.common.UpsertBannerRequest
import com.msa.eshop.backend.common.UpsertCustomerRequest
import com.msa.eshop.backend.common.UpsertDiscountRequest
import com.msa.eshop.backend.common.UpsertPaymentTermRequest
import com.msa.eshop.backend.common.UpsertProductGroupRequest
import com.msa.eshop.backend.common.UpsertProductRequest
import com.msa.eshop.backend.common.UserDto
import com.msa.eshop.backend.service.AdminService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val adminService: AdminService
) {
    @GetMapping("/dashboard")
    fun dashboard(): BaseResponse<DashboardDto> = BaseResponse(adminService.dashboard())

    @GetMapping("/customers")
    fun customers(): BaseResponse<List<UserDto>> = BaseResponse(adminService.customers())

    @PostMapping("/customers")
    fun createCustomer(@Valid @RequestBody request: UpsertCustomerRequest): BaseResponse<UserDto> =
        BaseResponse(adminService.createCustomer(request))

    @PutMapping("/customers/{id}")
    fun updateCustomer(@PathVariable id: UUID, @Valid @RequestBody request: UpsertCustomerRequest): BaseResponse<UserDto> =
        BaseResponse(adminService.updateCustomer(id, request))

    @DeleteMapping("/customers/{id}")
    fun deleteCustomer(@PathVariable id: UUID): BaseResponse<Boolean> {
        adminService.deleteCustomer(id)
        return BaseResponse(true)
    }

    @GetMapping("/addresses")
    fun addresses(@RequestParam(required = false) customerId: UUID?): BaseResponse<List<OrderAddressDto>> =
        BaseResponse(adminService.addresses(customerId))

    @PostMapping("/addresses")
    fun createAddress(@Valid @RequestBody request: UpsertAddressRequest): BaseResponse<OrderAddressDto> =
        BaseResponse(adminService.createAddress(request))

    @PutMapping("/addresses/{id}")
    fun updateAddress(@PathVariable id: UUID, @Valid @RequestBody request: UpsertAddressRequest): BaseResponse<OrderAddressDto> =
        BaseResponse(adminService.updateAddress(id, request))

    @DeleteMapping("/addresses/{id}")
    fun deleteAddress(@PathVariable id: UUID): BaseResponse<Boolean> {
        adminService.deleteAddress(id)
        return BaseResponse(true)
    }

    @GetMapping("/product-groups")
    fun productGroups(): BaseResponse<List<ProductGroupDto>> = BaseResponse(adminService.productGroups())

    @PostMapping("/product-groups")
    fun upsertProductGroup(@Valid @RequestBody request: UpsertProductGroupRequest): BaseResponse<ProductGroupDto> =
        BaseResponse(adminService.upsertProductGroup(request))

    @DeleteMapping("/product-groups/{code}")
    fun deleteProductGroup(@PathVariable code: Int): BaseResponse<Boolean> {
        adminService.deleteProductGroup(code)
        return BaseResponse(true)
    }

    @GetMapping("/products")
    fun products(): BaseResponse<List<ProductDto>> = BaseResponse(adminService.products())

    @PostMapping("/products")
    fun createProduct(@Valid @RequestBody request: UpsertProductRequest): BaseResponse<ProductDto> =
        BaseResponse(adminService.createProduct(request))

    @PutMapping("/products/{id}")
    fun updateProduct(@PathVariable id: UUID, @Valid @RequestBody request: UpsertProductRequest): BaseResponse<ProductDto> =
        BaseResponse(adminService.updateProduct(id, request))

    @DeleteMapping("/products/{id}")
    fun deleteProduct(@PathVariable id: UUID): BaseResponse<Boolean> {
        adminService.deleteProduct(id)
        return BaseResponse(true)
    }

    @GetMapping("/discounts")
    fun discounts(): BaseResponse<List<DiscountResultDto>> = BaseResponse(adminService.discounts())

    @PostMapping("/discounts")
    fun createDiscount(@Valid @RequestBody request: UpsertDiscountRequest): BaseResponse<DiscountResultDto> =
        BaseResponse(adminService.createDiscount(request))

    @PutMapping("/discounts/{id}")
    fun updateDiscount(@PathVariable id: UUID, @Valid @RequestBody request: UpsertDiscountRequest): BaseResponse<DiscountResultDto> =
        BaseResponse(adminService.updateDiscount(id, request))

    @DeleteMapping("/discounts/{id}")
    fun deleteDiscount(@PathVariable id: UUID): BaseResponse<Boolean> {
        adminService.deleteDiscount(id)
        return BaseResponse(true)
    }

    @GetMapping("/banners")
    fun banners(): BaseResponse<List<BannerDto>> = BaseResponse(adminService.banners())

    @PostMapping("/banners")
    fun createBanner(@Valid @RequestBody request: UpsertBannerRequest): BaseResponse<BannerDto> =
        BaseResponse(adminService.createBanner(request))

    @PutMapping("/banners/{id}")
    fun updateBanner(@PathVariable id: UUID, @Valid @RequestBody request: UpsertBannerRequest): BaseResponse<BannerDto> =
        BaseResponse(adminService.updateBanner(id, request))

    @DeleteMapping("/banners/{id}")
    fun deleteBanner(@PathVariable id: UUID): BaseResponse<Boolean> {
        adminService.deleteBanner(id)
        return BaseResponse(true)
    }

    @GetMapping("/payment-terms")
    fun paymentTerms(): BaseResponse<List<PaymentTermDto>> = BaseResponse(adminService.paymentTerms())

    @PostMapping("/payment-terms")
    fun createPaymentTerm(@Valid @RequestBody request: UpsertPaymentTermRequest): BaseResponse<PaymentTermDto> =
        BaseResponse(adminService.createPaymentTerm(request))

    @PutMapping("/payment-terms/{id}")
    fun updatePaymentTerm(@PathVariable id: UUID, @Valid @RequestBody request: UpsertPaymentTermRequest): BaseResponse<PaymentTermDto> =
        BaseResponse(adminService.updatePaymentTerm(id, request))

    @DeleteMapping("/payment-terms/{id}")
    fun deletePaymentTerm(@PathVariable id: UUID): BaseResponse<Boolean> {
        adminService.deletePaymentTerm(id)
        return BaseResponse(true)
    }
}
