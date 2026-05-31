package com.msa.eshop.backend.common.dtos

import java.time.OffsetDateTime
import java.util.UUID

data class AuditLogDto(
    val id: UUID?,
    val actorCustomerId: UUID?,
    val actorCustomerCode: String?,
    val actorCustomerName: String?,
    val action: String,
    val entityType: String,
    val entityId: String?,
    val oldValue: Map<String, Any?>?,
    val newValue: Map<String, Any?>?,
    val ipAddress: String?,
    val userAgent: String?,
    val description: String?,
    val createdAt: OffsetDateTime
)