package com.msa.eshop.backend.common.dtos

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "کد مشتری الزامی است")
    val customerCode: String,

    @field:NotBlank(message = "رمز عبور الزامی است")
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val passwordChangeRequired: Boolean
)

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token الزامی است")
    val refreshToken: String
)

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val passwordChangeRequired: Boolean
)

data class LogoutRequest(
    @field:NotBlank(message = "Refresh token الزامی است")
    val refreshToken: String
)

data class ChangePasswordRequest(
    @field:NotBlank(message = "رمز عبور فعلی الزامی است")
    val oldPassword: String,

    @field:NotBlank(message = "رمز عبور جدید الزامی است")
    val newPassword: String
)