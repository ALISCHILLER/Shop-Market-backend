package com.msa.eshop.backend.common

import org.springframework.http.HttpStatus

open class ApiException(
    val status: HttpStatus,
    override val message: String
) : RuntimeException(message)

class NotFoundException(message: String) : ApiException(HttpStatus.NOT_FOUND, message)
class BadRequestException(message: String) : ApiException(HttpStatus.BAD_REQUEST, message)
class UnauthorizedException(message: String = "نیاز به ورود مجدد دارید") : ApiException(HttpStatus.UNAUTHORIZED, message)
class ForbiddenException(message: String = "شما مجوز دسترسی به این بخش را ندارید") : ApiException(HttpStatus.FORBIDDEN, message)
