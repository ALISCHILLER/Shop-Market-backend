package com.msa.eshop.backend.api.admin


import com.msa.eshop.backend.common.dtos.BaseResponse
import com.msa.eshop.backend.common.dtos.DashboardDto
import com.msa.eshop.backend.service.admin.AdminDashboardService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin")
class AdminDashboardController(
    private val dashboardService: AdminDashboardService
) {
    @GetMapping("/dashboard")
    fun dashboard(): BaseResponse<DashboardDto> =
        BaseResponse(dashboardService.dashboard())
}