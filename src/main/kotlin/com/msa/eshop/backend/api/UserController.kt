package com.msa.eshop.backend.api

import com.msa.eshop.backend.common.ChangePasswordRequest
import com.msa.eshop.backend.common.ChangePasswordResponse
import com.msa.eshop.backend.common.TokenRequest
import com.msa.eshop.backend.common.TokenResponse
import com.msa.eshop.backend.common.UserResponse
import com.msa.eshop.backend.service.AuthService
import com.msa.eshop.backend.service.CurrentUserService
import com.msa.eshop.backend.service.toDto
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/User")
class UserController(
    private val authService: AuthService,
    private val currentUserService: CurrentUserService
) {
    @PostMapping("/loginUser")
    fun login(@RequestBody request: TokenRequest): TokenResponse = authService.login(request)

    @GetMapping("/CustomerProfile")
    fun profile(): UserResponse = UserResponse(listOf(currentUserService.requireCustomer().toDto()))

    @PostMapping("/changepassword")
    fun changePassword(@Valid @RequestBody request: ChangePasswordRequest): ChangePasswordResponse =
        ChangePasswordResponse(authService.changePassword(request))
}
