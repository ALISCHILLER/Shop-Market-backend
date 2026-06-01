package com.msa.eshop.backend.api

import com.msa.eshop.backend.common.BaseResponse
import com.msa.eshop.backend.common.dtos.ChangePasswordRequest
import com.msa.eshop.backend.common.dtos.LogoutRequest
import com.msa.eshop.backend.common.dtos.RefreshTokenRequest
import com.msa.eshop.backend.common.dtos.RefreshTokenResponseDto
import com.msa.eshop.backend.common.dtos.TokenRequest
import com.msa.eshop.backend.common.dtos.UserDto
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
@RequestMapping("/api/v1/auth")
class AuthRestController(
    private val authService: AuthService,
    private val currentUserService: CurrentUserService,
    private val clientIpResolver: ClientIpResolver
) {

    @PostMapping("/login")
    fun login(
        @RequestBody request: TokenRequest,
        httpRequest: HttpServletRequest
    ): BaseResponse<Any> =
        BaseResponse(
            data = authService.login(
                request = request,
                ipAddress = clientIpResolver.resolve(httpRequest),
                userAgent = httpRequest.getHeader("User-Agent")
            )
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
            )
        )

    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody request: LogoutRequest
    ): BaseResponse<Boolean> =
        BaseResponse(
            data = authService.logout(request)
        )

    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest
    ): BaseResponse<Boolean> =
        BaseResponse(
            data = authService.changePassword(request)
        )

    @GetMapping("/me")
    fun me(): BaseResponse<UserDto> =
        BaseResponse(
            data = currentUserService.requireCustomer().toDto()
        )
}