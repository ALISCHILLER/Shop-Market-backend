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
import com.msa.eshop.backend.service.admin.AdminAddressService
import com.msa.eshop.backend.service.admin.AdminBannerService
import com.msa.eshop.backend.service.admin.AdminCustomerService
import com.msa.eshop.backend.service.admin.AdminDashboardService
import com.msa.eshop.backend.service.admin.AdminDiscountService
import com.msa.eshop.backend.service.admin.AdminPaymentTermService
import com.msa.eshop.backend.service.admin.AdminProductGroupService
import com.msa.eshop.backend.service.admin.AdminProductService
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
import com.msa.eshop.backend.common.AdminCartSummaryDto
import com.msa.eshop.backend.common.PageResponseDto
import com.msa.eshop.backend.common.ReportCartDetailsDto
import com.msa.eshop.backend.common.UpdateCartStatusRequest
import com.msa.eshop.backend.service.admin.AdminCartService

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val dashboardService: AdminDashboardService,
    private val customerService: AdminCustomerService,
    private val addressService: AdminAddressService,
    private val productGroupService: AdminProductGroupService,
    private val productService: AdminProductService,
    private val discountService: AdminDiscountService,
    private val bannerService: AdminBannerService,
    private val paymentTermService: AdminPaymentTermService,
    private val cartService: AdminCartService,
) {
    @GetMapping("/dashboard")
    fun dashboard(): BaseResponse<DashboardDto> =
        BaseResponse(dashboardService.dashboard())

    @GetMapping("/customers")
    fun customers(): BaseResponse<List<UserDto>> =
        BaseResponse(customerService.findAll())

    @PostMapping("/customers")
    fun createCustomer(
        @Valid @RequestBody request: UpsertCustomerRequest
    ): BaseResponse<UserDto> =
        BaseResponse(customerService.create(request))

    @PutMapping("/customers/{id}")
    fun updateCustomer(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpsertCustomerRequest
    ): BaseResponse<UserDto> =
        BaseResponse(customerService.update(id, request))

    @DeleteMapping("/customers/{id}")
    fun deleteCustomer(
        @PathVariable id: UUID
    ): BaseResponse<Boolean> {
        customerService.delete(id)
        return BaseResponse(true)
    }

    @GetMapping("/addresses")
    fun addresses(
        @RequestParam(required = false) customerId: UUID?
    ): BaseResponse<List<OrderAddressDto>> =
        BaseResponse(addressService.findAll(customerId))

    @PostMapping("/addresses")
    fun createAddress(
        @Valid @RequestBody request: UpsertAddressRequest
    ): BaseResponse<OrderAddressDto> =
        BaseResponse(addressService.create(request))

    @PutMapping("/addresses/{id}")
    fun updateAddress(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpsertAddressRequest
    ): BaseResponse<OrderAddressDto> =
        BaseResponse(addressService.update(id, request))

    @DeleteMapping("/addresses/{id}")
    fun deleteAddress(
        @PathVariable id: UUID
    ): BaseResponse<Boolean> {
        addressService.delete(id)
        return BaseResponse(true)
    }

    @GetMapping("/product-groups")
    fun productGroups(): BaseResponse<List<ProductGroupDto>> =
        BaseResponse(productGroupService.findAll())

    @PostMapping("/product-groups")
    fun upsertProductGroup(
        @Valid @RequestBody request: UpsertProductGroupRequest
    ): BaseResponse<ProductGroupDto> =
        BaseResponse(productGroupService.upsert(request))

    @DeleteMapping("/product-groups/{code}")
    fun deleteProductGroup(
        @PathVariable code: Int
    ): BaseResponse<Boolean> {
        productGroupService.delete(code)
        return BaseResponse(true)
    }

    @GetMapping("/products")
    fun products(): BaseResponse<List<ProductDto>> =
        BaseResponse(productService.findAll())

    @PostMapping("/products")
    fun createProduct(
        @Valid @RequestBody request: UpsertProductRequest
    ): BaseResponse<ProductDto> =
        BaseResponse(productService.create(request))

    @PutMapping("/products/{id}")
    fun updateProduct(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpsertProductRequest
    ): BaseResponse<ProductDto> =
        BaseResponse(productService.update(id, request))

    @DeleteMapping("/products/{id}")
    fun deleteProduct(
        @PathVariable id: UUID
    ): BaseResponse<Boolean> {
        productService.delete(id)
        return BaseResponse(true)
    }

    @GetMapping("/discounts")
    fun discounts(): BaseResponse<List<DiscountResultDto>> =
        BaseResponse(discountService.findAll())

    @PostMapping("/discounts")
    fun createDiscount(
        @Valid @RequestBody request: UpsertDiscountRequest
    ): BaseResponse<DiscountResultDto> =
        BaseResponse(discountService.create(request))

    @PutMapping("/discounts/{id}")
    fun updateDiscount(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpsertDiscountRequest
    ): BaseResponse<DiscountResultDto> =
        BaseResponse(discountService.update(id, request))

    @DeleteMapping("/discounts/{id}")
    fun deleteDiscount(
        @PathVariable id: UUID
    ): BaseResponse<Boolean> {
        discountService.delete(id)
        return BaseResponse(true)
    }

    @GetMapping("/banners")
    fun banners(): BaseResponse<List<BannerDto>> =
        BaseResponse(bannerService.findAll())

    @PostMapping("/banners")
    fun createBanner(
        @Valid @RequestBody request: UpsertBannerRequest
    ): BaseResponse<BannerDto> =
        BaseResponse(bannerService.create(request))

    @PutMapping("/banners/{id}")
    fun updateBanner(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpsertBannerRequest
    ): BaseResponse<BannerDto> =
        BaseResponse(bannerService.update(id, request))

    @DeleteMapping("/banners/{id}")
    fun deleteBanner(
        @PathVariable id: UUID
    ): BaseResponse<Boolean> {
        bannerService.delete(id)
        return BaseResponse(true)
    }

    @GetMapping("/payment-terms")
    fun paymentTerms(): BaseResponse<List<PaymentTermDto>> =
        BaseResponse(paymentTermService.findAll())

    @PostMapping("/payment-terms")
    fun createPaymentTerm(
        @Valid @RequestBody request: UpsertPaymentTermRequest
    ): BaseResponse<PaymentTermDto> =
        BaseResponse(paymentTermService.create(request))

    @PutMapping("/payment-terms/{id}")
    fun updatePaymentTerm(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpsertPaymentTermRequest
    ): BaseResponse<PaymentTermDto> =
        BaseResponse(paymentTermService.update(id, request))

    @DeleteMapping("/payment-terms/{id}")
    fun deletePaymentTerm(
        @PathVariable id: UUID
    ): BaseResponse<Boolean> {
        paymentTermService.delete(id)
        return BaseResponse(true)
    }

    @GetMapping("/carts")
    fun carts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) cartCode: Int?,
        @RequestParam(required = false) customerSearch: String?,
        @RequestParam(required = false) fromDate: String?,
        @RequestParam(required = false) toDate: String?
    ): BaseResponse<PageResponseDto<AdminCartSummaryDto>> =
        BaseResponse(
            cartService.findAll(
                page = page,
                size = size,
                cartCode = cartCode,
                customerSearch = customerSearch,
                fromDate = fromDate,
                toDate = toDate
            )
        )

    @GetMapping("/carts/{cartCode}")
    fun cartDetails(
        @PathVariable cartCode: Int
    ): BaseResponse<List<ReportCartDetailsDto>> =
        BaseResponse(cartService.details(cartCode))

    @PutMapping("/carts/{cartCode}/status")
    fun updateCartStatus(
        @PathVariable cartCode: Int,
        @Valid @RequestBody request: UpdateCartStatusRequest
    ): BaseResponse<AdminCartSummaryDto> =
        BaseResponse(cartService.updateStatus(cartCode, request))

    @GetMapping("/customers/page")
    fun customersPage(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) role: String?,
        @RequestParam(required = false) enabled: Boolean?
    ): BaseResponse<PageResponseDto<UserDto>> =
        BaseResponse(
            customerService.search(
                page = page,
                size = size,
                search = search,
                role = role,
                enabled = enabled
            )
        )

    @GetMapping("/products/page")
    fun productsPage(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) productGroupCode: Int?,
        @RequestParam(required = false) isDiscounts: Boolean?,
        @RequestParam(required = false) isTax: Boolean?
    ): BaseResponse<PageResponseDto<ProductDto>> =
        BaseResponse(
            productService.search(
                page = page,
                size = size,
                search = search,
                productGroupCode = productGroupCode,
                isDiscounts = isDiscounts,
                isTax = isTax
            )
        )
}