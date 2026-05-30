package com.msa.eshop.backend.common.dtos

import jakarta.validation.constraints.NotBlank

data class UserDto(
    val id: String,
    val customerCode: String,
    val customerName: String?,
    val mobile: String?,
    val phone: String?,
    val center: String?,
    val nationalCode: String?,
    val password: String? = null,
    val salt: String? = null,
    val role: String = "CUSTOMER",
    val enabled: Boolean = true
)

data class UpsertCustomerRequest(
    @field:NotBlank(message = "کد مشتری الزامی است")
    val customerCode: String,

    @field:NotBlank(message = "نام مشتری الزامی است")
    val customerName: String,

    val mobile: String? = null,
    val phone: String? = null,
    val center: String? = null,
    val nationalCode: String? = null,
    val password: String? = null,
    val role: String = "CUSTOMER",
    val enabled: Boolean = true
)

data class OrderAddressDto(
    val centerName: String,
    val customerAddress: String,
    val customerMobile: String,
    val customerPhone: String,
    val id: String,
    val customerId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val lat: Double? = latitude,
    val lng: Double? = longitude,
    val isDefault: Boolean = false
)

data class UpsertAddressRequest(
    @field:NotBlank(message = "شناسه مشتری الزامی است")
    val customerId: String,

    val centerName: String = "",

    @field:NotBlank(message = "آدرس الزامی است")
    val customerAddress: String,

    val customerMobile: String = "",
    val customerPhone: String = "",

    val latitude: Double? = null,
    val longitude: Double? = null,
    val lat: Double? = null,
    val lng: Double? = null,

    val isDefault: Boolean? = null
)