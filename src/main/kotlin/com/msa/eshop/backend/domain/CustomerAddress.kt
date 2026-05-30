package com.msa.eshop.backend.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Check

@Entity
@Table(
    name = "customer_addresses",
    indexes = [
        Index(name = "idx_customer_addresses_customer_id", columnList = "customer_id"),
        Index(name = "idx_customer_addresses_location", columnList = "latitude, longitude")
    ]
)
@Check(
    constraints = "(latitude is null and longitude is null) or (latitude between -90 and 90 and longitude between -180 and 180)"
)
open class CustomerAddress(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "customer_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_customer_addresses_customer")
    )
    open var customer: Customer? = null,

    @Column(name = "center_name", nullable = false, length = 255)
    open var centerName: String = "",

    @Column(name = "customer_address", nullable = false, columnDefinition = "text")
    open var customerAddress: String = "",

    @Column(name = "customer_mobile", nullable = false, length = 32)
    open var customerMobile: String = "",

    @Column(name = "customer_phone", nullable = false, length = 32)
    open var customerPhone: String = "",

    @Column(name = "latitude")
    open var latitude: Double? = null,

    @Column(name = "longitude")
    open var longitude: Double? = null,

    @Column(name = "is_default", nullable = false)
    open var isDefault: Boolean = false
) : AuditableUuidEntity()