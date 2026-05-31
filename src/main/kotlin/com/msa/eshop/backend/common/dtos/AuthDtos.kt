package com.msa.eshop.backend.common.dtos

import jakarta.validation.constraints.NotBlank

data class TokenRequest(
    val customerCode: String? = null,
    val password: String? = null
)

data class LoginDataDto(
    val token: String,
    val refreshToken: String,
    val passwordChangeRequired: Boolean
)

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token الزامی است")
    val refreshToken: String
)

data class RefreshTokenResponseDto(
    val token: String,
    val refreshToken: String,
    val passwordChangeRequired: Boolean
)

data class LogoutRequest(
    @field:NotBlank(message = "Refresh token الزامی است")
    val refreshToken: String
)

data class ChangePasswordRequest(
    @field:NotBlank(message = "رمز عبور فعلی را وارد کنید")
    val oldPassword: String,

    @field:NotBlank(message = "رمز عبور جدید را وارد کنید")
    val newPassword: String
)