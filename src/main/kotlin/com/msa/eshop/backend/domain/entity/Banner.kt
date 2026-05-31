package com.msa.eshop.backend.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "banners",
    indexes = [
        Index(name = "idx_banners_name", columnList = "banner_name")
    ]
)
open class Banner(
    @Column(name = "banner_image", nullable = false, columnDefinition = "text")
    open var bannerImage: String = "",

    @Column(name = "banner_name", nullable = false, length = 255)
    open var bannerName: String = ""
) : AuditableUuidEntity()