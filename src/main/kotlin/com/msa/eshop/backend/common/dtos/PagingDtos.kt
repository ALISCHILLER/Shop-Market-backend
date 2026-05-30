package com.msa.eshop.backend.common.dtos

data class PageMetaDto(
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

data class PageResponseDto<T>(
    val items: List<T>,
    val meta: PageMetaDto
)