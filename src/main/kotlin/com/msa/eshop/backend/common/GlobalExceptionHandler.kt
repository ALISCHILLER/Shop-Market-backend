package com.msa.eshop.backend.common

import jakarta.validation.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(exception: ApiException): ResponseEntity<BaseResponse<Nothing>> =
        ResponseEntity.status(exception.status).body(BaseResponse(data = null, hasError = true, message = exception.message))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(exception: MethodArgumentNotValidException): ResponseEntity<BaseResponse<Nothing>> {
        val message = exception.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
            ?: "اطلاعات ارسال‌شده معتبر نیست"
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(BaseResponse(data = null, hasError = true, message = message))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(exception: ConstraintViolationException): ResponseEntity<BaseResponse<Nothing>> {
        val message = exception.constraintViolations.firstOrNull()?.message ?: "اطلاعات ارسال‌شده معتبر نیست"
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(BaseResponse(data = null, hasError = true, message = message))
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(exception: DataIntegrityViolationException): ResponseEntity<BaseResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(BaseResponse(data = null, hasError = true, message = "این رکورد قبلاً ثبت شده یا به داده‌های دیگری وابسته است"))

    @ExceptionHandler(Exception::class)
    fun handleUnknown(exception: Exception): ResponseEntity<BaseResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(BaseResponse(data = null, hasError = true, message = "خطای سرور رخ داد"))
}
