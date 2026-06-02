package com.msa.eshop.backend.common

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.ALWAYS)
data class BaseResponse<T>(
    val data: T?,
    val hasError: Boolean = false,
    val message: String? = null
)