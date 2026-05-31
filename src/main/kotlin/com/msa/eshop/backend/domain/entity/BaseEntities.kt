package com.msa.eshop.backend.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Version
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.OffsetDateTime
import java.util.UUID

@MappedSuperclass
abstract class BaseUuidEntity {
    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    open var id: UUID? = null

    fun requireId(): UUID =
        requireNotNull(id) { "Entity id is not assigned yet" }
}

@MappedSuperclass
abstract class AuditableUuidEntity : BaseUuidEntity() {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    open var createdAt: OffsetDateTime = OffsetDateTime.now()

    @UpdateTimestamp
    @Column(name = "updated_at")
    open var updatedAt: OffsetDateTime? = null

    @Version
    @Column(name = "version", nullable = false)
    open var version: Long = 0
}