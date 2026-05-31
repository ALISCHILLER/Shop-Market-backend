package com.msa.eshop.backend.api.admin

import com.msa.eshop.backend.common.BaseResponse
import com.msa.eshop.backend.common.dtos.AuditLogDto
import com.msa.eshop.backend.common.dtos.PageResponseDto
import com.msa.eshop.backend.service.audit.AuditLogService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
class AdminAuditLogController(
    private val auditLogService: AuditLogService
) {

    @GetMapping("/page")
    fun page(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) action: String?,
        @RequestParam(required = false) entityType: String?,
        @RequestParam(required = false) entityId: String?,
        @RequestParam(required = false) actorCustomerCode: String?,
        @RequestParam(required = false) fromDate: OffsetDateTime?,
        @RequestParam(required = false) toDate: OffsetDateTime?,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") direction: String
    ): BaseResponse<PageResponseDto<AuditLogDto>> =
        BaseResponse(
            auditLogService.search(
                page = page,
                size = size,
                action = action,
                entityType = entityType,
                entityId = entityId,
                actorCustomerCode = actorCustomerCode,
                fromDate = fromDate,
                toDate = toDate,
                sortBy = sortBy,
                direction = direction
            )
        )
}