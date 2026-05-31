package com.msa.eshop.backend.domain.repository

import com.msa.eshop.backend.domain.entity.AuditLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.UUID

interface AuditLogRepository : JpaRepository<AuditLog, UUID> {

    @Query(
        """
        select a from AuditLog a
        where (:action is null or upper(a.action) = upper(:action))
          and (:entityType is null or upper(a.entityType) = upper(:entityType))
          and (:entityId is null or a.entityId = :entityId)
          and (:actorCustomerCode is null
               or lower(coalesce(a.actorCustomerCode, '')) like lower(concat('%', :actorCustomerCode, '%')))
          and (:fromDate is null or a.createdAt >= :fromDate)
          and (:toDate is null or a.createdAt <= :toDate)
        """
    )
    fun searchAuditLogs(
        @Param("action") action: String?,
        @Param("entityType") entityType: String?,
        @Param("entityId") entityId: String?,
        @Param("actorCustomerCode") actorCustomerCode: String?,
        @Param("fromDate") fromDate: OffsetDateTime?,
        @Param("toDate") toDate: OffsetDateTime?,
        pageable: Pageable
    ): Page<AuditLog>
}