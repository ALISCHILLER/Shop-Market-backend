package com.msa.eshop.backend.common.dtos

import jakarta.validation.constraints.NotBlank

data class TokenRequest(
    val customerCode: String? = null,
    val password: String? = null
)

data class ChangePasswordRequest(
    @field:NotBlank(message = "رمز عبور فعلی را وارد کنید")
    val oldPassword: String,

    @field:NotBlank(message = "رمز عبور جدید را وارد کنید")
    val newPassword: String
)