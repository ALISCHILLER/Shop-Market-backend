package com.msa.eshop.backend.api

import com.msa.eshop.backend.common.BaseResponse
import com.msa.eshop.backend.common.TokenResponse
import com.msa.eshop.backend.common.UserResponse
import com.msa.eshop.backend.common.dtos.ChangePasswordRequest
import com.msa.eshop.backend.common.dtos.LogoutRequest
import com.msa.eshop.backend.common.dtos.RefreshTokenRequest
import com.msa.eshop.backend.common.dtos.RefreshTokenResponseDto
import com.msa.eshop.backend.common.dtos.TokenRequest
import com.msa.eshop.backend.security.ClientIpResolver
import com.msa.eshop.backend.service.AuthService
import com.msa.eshop.backend.service.CurrentUserService
import com.msa.eshop.backend.service.toDto
import jakarta.servlet.http.HttpServletRequest
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
    private val currentUserService: CurrentUserService,
    private val clientIpResolver: ClientIpResolver
) {

    @PostMapping("/loginUser")
    fun login(
        @RequestBody request: TokenRequest,
        httpRequest: HttpServletRequest
    ): TokenResponse {
        val loginData = authService.login(
            request = request,
            ipAddress = clientIpResolver.resolve(httpRequest),
            userAgent = httpRequest.getHeader("User-Agent")
        )

        return TokenResponse(
            token = loginData.token,
            refreshToken = loginData.refreshToken,
            passwordChangeRequired = loginData.passwordChangeRequired,
            data = loginData
        )
    }

    @GetMapping("/CustomerProfile")
    fun profile(): UserResponse =
        UserResponse(
            user = listOf(currentUserService.requireCustomer().toDto())
        )

    @PostMapping("/changepassword")
    fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest
    ): BaseResponse<Boolean> =
        BaseResponse(
            data = authService.changePassword(request),
            hasError = false,
            message = null
        )

    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshTokenRequest,
        httpRequest: HttpServletRequest
    ): BaseResponse<RefreshTokenResponseDto> =
        BaseResponse(
            data = authService.refreshToken(
                request = request,
                ipAddress = clientIpResolver.resolve(httpRequest),
                userAgent = httpRequest.getHeader("User-Agent")
            ),
            hasError = false,
            message = null
        )

    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody request: LogoutRequest
    ): BaseResponse<Boolean> =
        BaseResponse(
            data = authService.logout(request),
            hasError = false,
            message = null
        )
}