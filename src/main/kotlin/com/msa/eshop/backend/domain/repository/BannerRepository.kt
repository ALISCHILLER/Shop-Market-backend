package com.msa.eshop.backend.domain.repository

import com.msa.eshop.backend.domain.entity.Banner
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BannerRepository : JpaRepository<Banner, UUID> {

    fun findAllByOrderByBannerNameAsc(): List<Banner>
}