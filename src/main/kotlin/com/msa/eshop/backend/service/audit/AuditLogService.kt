package com.msa.eshop.backend.service.audit

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.eshop.backend.common.cleanOrNull
import com.msa.eshop.backend.common.createPageable
import com.msa.eshop.backend.common.dtos.AuditLogDto
import com.msa.eshop.backend.common.dtos.PageResponseDto
import com.msa.eshop.backend.common.toPageResponse
import com.msa.eshop.backend.domain.AuditLog
import com.msa.eshop.backend.domain.AuditLogRepository
import com.msa.eshop.backend.domain.Customer
import com.msa.eshop.backend.service.CurrentUserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.OffsetDateTime

@Service
class AuditLogService(
    private val auditLogRepository: AuditLogRepository,
    private val currentUserService: CurrentUserService,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    fun record(
        action: String,
        entityType: String,
        entityId: String?,
        oldValue: Any? = null,
        newValue: Any? = null,
        description: String? = null
    ) {
        val actor = currentActorOrNull()
        val request = currentRequestOrNull()

        val auditLog = AuditLog(
            actorCustomerId = actor?.id,
            actorCustomerCode = actor?.customerCode,
            actorCustomerName = actor?.customerName,

            action = action.trim().uppercase(),
            entityType = entityType.trim(),
            entityId = entityId?.trim()?.takeIf { it.isNotBlank() },

            oldValue = normalizeValue(oldValue),
            newValue = normalizeValue(newValue),

            ipAddress = extractIpAddress(request),
            userAgent = request?.getHeader("User-Agent")?.take(MAX_USER_AGENT_LENGTH),

            description = description?.trim()?.takeIf { it.isNotBlank() }
        )

        auditLogRepository.save(auditLog)
    }

    @Transactional(readOnly = true)
    fun search(
        page: Int,
        size: Int,
        action: String?,
        entityType: String?,
        entityId: String?,
        actorCustomerCode: String?,
        fromDate: OffsetDateTime?,
        toDate: OffsetDateTime?,
        sortBy: String = DEFAULT_SORT_BY,
        direction: String = DEFAULT_SORT_DIRECTION
    ): PageResponseDto<AuditLogDto> {
        val pageable = createPageable(
            page = page,
            size = size,
            sortBy = sortBy,
            direction = direction,
            allowedSorts = ALLOWED_SORTS
        )

        return auditLogRepository.searchAuditLogs(
            action = action.cleanOrNull(),
            entityType = entityType.cleanOrNull(),
            entityId = entityId.cleanOrNull(),
            actorCustomerCode = actorCustomerCode.cleanOrNull(),
            fromDate = fromDate,
            toDate = toDate,
            pageable = pageable
        ).toPageResponse { it.toDto() }
    }

    fun snapshotOf(
        vararg values: Pair<String, Any?>
    ): Map<String, Any?> {
        return values.toMap()
    }

    private fun currentActorOrNull(): Customer? {
        return runCatching {
            currentUserService.requireCustomer()
        }.getOrNull()
    }

    private fun currentRequestOrNull(): HttpServletRequest? {
        val attributes = RequestContextHolder.getRequestAttributes()
                as? ServletRequestAttributes

        return attributes?.request
    }

    private fun extractIpAddress(request: HttpServletRequest?): String? {
        if (request == null) return null

        val forwardedFor = request.getHeader("X-Forwarded-For")
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        if (forwardedFor != null) {
            return forwardedFor.take(MAX_IP_LENGTH)
        }

        return request.remoteAddr
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.take(MAX_IP_LENGTH)
    }

    private fun normalizeValue(value: Any?): Map<String, Any?>? {
        if (value == null) return null

        if (value is Map<*, *>) {
            return value.entries.associate { entry ->
                entry.key.toString() to entry.value
            }
        }

        return objectMapper.convertValue(
            value,
            object : TypeReference<Map<String, Any?>>() {}
        )
    }

    private fun AuditLog.toDto(): AuditLogDto {
        return AuditLogDto(
            id = id,
            actorCustomerId = actorCustomerId,
            actorCustomerCode = actorCustomerCode,
            actorCustomerName = actorCustomerName,
            action = action,
            entityType = entityType,
            entityId = entityId,
            oldValue = oldValue,
            newValue = newValue,
            ipAddress = ipAddress,
            userAgent = userAgent,
            description = description,
            createdAt = createdAt
        )
    }

    private companion object {
        const val DEFAULT_SORT_BY = "createdAt"
        const val DEFAULT_SORT_DIRECTION = "DESC"
        const val MAX_IP_LENGTH = 64
        const val MAX_USER_AGENT_LENGTH = 500

        val ALLOWED_SORTS = setOf(
            "createdAt",
            "action",
            "entityType",
            "actorCustomerCode"
        )
    }
}