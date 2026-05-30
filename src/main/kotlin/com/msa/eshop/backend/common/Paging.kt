package com.msa.eshop.backend.common

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

private const val DEFAULT_PAGE_SIZE = 20
private const val MAX_PAGE_SIZE = 100

fun createPageable(
    page: Int,
    size: Int,
    sortBy: String = "createdAt",
    direction: Sort.Direction = Sort.Direction.DESC
): Pageable {
    val normalizedPage = page.coerceAtLeast(0)
    val normalizedSize = size.coerceIn(1, MAX_PAGE_SIZE)

    return PageRequest.of(
        normalizedPage,
        normalizedSize,
        Sort.by(direction, sortBy)
    )
}

fun <T, R> Page<T>.toPageResponse(mapper: (T) -> R): PageResponseDto<R> =
    PageResponseDto(
        items = content.map(mapper),
        meta = PageMetaDto(
            page = number,
            size = size,
            totalItems = totalElements,
            totalPages = totalPages,
            hasNext = hasNext(),
            hasPrevious = hasPrevious()
        )
    )