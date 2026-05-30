package com.msa.eshop.backend.common

import com.msa.eshop.backend.common.dtos.BaseResponse
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ApiException::class)
    fun handleApiException(exception: ApiException): ResponseEntity<BaseResponse<Nothing>> =
        ResponseEntity.status(exception.status)
            .body(BaseResponse(data = null, hasError = true, message = exception.message))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(exception: MethodArgumentNotValidException): ResponseEntity<BaseResponse<Nothing>> {
        val message = exception.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
            ?: "اطلاعات ارسال‌شده معتبر نیست"

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(BaseResponse(data = null, hasError = true, message = message))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(exception: ConstraintViolationException): ResponseEntity<BaseResponse<Nothing>> {
        val message = exception.constraintViolations.firstOrNull()?.message
            ?: "اطلاعات ارسال‌شده معتبر نیست"

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(BaseResponse(data = null, hasError = true, message = message))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(exception: MethodArgumentTypeMismatchException): ResponseEntity<BaseResponse<Nothing>> {
        val name = exception.name

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse(data = null, hasError = true, message = "مقدار پارامتر $name معتبر نیست"))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(exception: MissingServletRequestParameterException): ResponseEntity<BaseResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse(data = null, hasError = true, message = "پارامتر ${exception.parameterName} الزامی است"))

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(exception: HttpMessageNotReadableException): ResponseEntity<BaseResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse(data = null, hasError = true, message = "بدنه درخواست معتبر نیست"))

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(exception: DataIntegrityViolationException): ResponseEntity<BaseResponse<Nothing>> {
        logger.warn("Data integrity violation", exception)

        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(
                BaseResponse(
                    data = null,
                    hasError = true,
                    message = "این رکورد قبلاً ثبت شده یا به داده‌های دیگری وابسته است"
                )
            )
    }

    @ExceptionHandler(ArithmeticException::class)
    fun handleArithmetic(exception: ArithmeticException): ResponseEntity<BaseResponse<Nothing>> {
        logger.warn("Arithmetic error", exception)

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                BaseResponse(
                    data = null,
                    hasError = true,
                    message = "مقدار عددی ارسال‌شده یا محاسبه‌شده بیش از حد مجاز است"
                )
            )
    }
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException::class)
    fun handleOptimisticLock(
        exception: org.springframework.orm.ObjectOptimisticLockingFailureException
    ): ResponseEntity<BaseResponse<Nothing>> {
        logger.warn("Optimistic lock failure", exception)

        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(
                BaseResponse(
                    data = null,
                    hasError = true,
                    message = "این اطلاعات توسط کاربر یا عملیات دیگری تغییر کرده است. لطفاً صفحه را به‌روزرسانی کنید"
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnknown(exception: Exception): ResponseEntity<BaseResponse<Nothing>> {
        logger.error("Unhandled backend error", exception)

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(BaseResponse(data = null, hasError = true, message = "خطای سرور رخ داد"))
    }
}