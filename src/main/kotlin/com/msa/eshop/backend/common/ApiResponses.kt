package com.msa.eshop.backend.common.dtos

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.ALWAYS)
data class BaseResponse<T>(
    val data: T?,
    val hasError: Boolean = false,
    val message: String? = null
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class TokenResponse(
    val token: String?,
    val data: String? = token,
    val hasError: Boolean = false,
    val message: String? = null
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class UserResponse(
    val user: List<UserDto>,
    val data: List<UserDto> = user,
    val hasError: Boolean = false,
    val message: String? = null
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class ProductResponse(
    val products: List<ProductDto>,
    val data: List<ProductDto> = products,
    val hasError: Boolean = false,
    val message: String? = null
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class ProductGroupResponse(
    val productGroups: List<ProductGroupDto>,
    val data: List<ProductGroupDto> = productGroups,
    val hasError: Boolean = false,
    val message: String? = null
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class BannerResponse(
    val banners: List<BannerDto>,
    val data: List<BannerDto> = banners,
    val hasError: Boolean = false,
    val message: String? = null
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class DiscountResponse(
    val discountResultModel: List<DiscountResultDto>,
    val data: List<DiscountResultDto> = discountResultModel,
    val hasError: Boolean = false,
    val message: String? = null
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class SimulateResultModel(
    val simulateModel: List<SimulateDto>,
    val data: List<SimulateDto> = simulateModel,
    val hasError: Boolean = false,
    val message: String? = null
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class OrderAddressResultModel(
    val orderaddress: List<OrderAddressDto>,
    val data: List<OrderAddressDto> = orderaddress,
    val hasError: Boolean = false,
    val message: String? = null
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class PaymentTermResponse(
    val paymentTerm: List<PaymentTermDto>,
    val data: List<PaymentTermDto> = paymentTerm,
    val hasError: Boolean = false,
    val message: String? = null
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class InsertCartModelResponse(
    val insertCart: Boolean,
    val data: Boolean = insertCart,
    val hasError: Boolean = false,
    val message: String? = null
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class ChangePasswordResponse(
    val changePassword: Boolean,
    val insertCart: Boolean = changePassword,
    val data: Boolean = changePassword,
    val hasError: Boolean = false,
    val message: String? = null
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class ReportHistoryCustomerResponse(
    val reportHistoryCustomer: List<ReportHistoryCustomerDto>,
    val data: List<ReportHistoryCustomerDto> = reportHistoryCustomer,
    val hasError: Boolean = false,
    val message: String? = null
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class ReportCartDetailsResponse(
    val reportCartDetails: List<ReportCartDetailsDto>,
    val data: List<ReportCartDetailsDto> = reportCartDetails,
    val hasError: Boolean = false,
    val message: String? = null
)