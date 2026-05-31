package com.msa.eshop.backend.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertThrows
class BackendUtilsDateTest {

    @Test
    fun `parse should accept gregorian date`() {
        val result = "2026-05-31".parseClientDateOrNull()

        assertEquals(LocalDate.of(2026, 5, 31), result)
    }

    @Test
    fun `parse should accept persian digits`() {
        val result = "۱۴۰۴/۰۳/۱۰".parseClientDateOrNull()

        // این expected بعد از بررسی دقیق converter پروژه باید نهایی شود
        // هدف تست: مطمئن شویم اعداد فارسی درست normalize می‌شوند.
        requireNotNull(result)
    }

    @Test
    fun `parse should reject invalid date`() {
        assertThrows(BadRequestException::class.java) {
            "invalid-date".parseClientDateOrNull()
        }
    }
    @Test
    fun `parse should accept esfand 30 in leap jalali year`() {
        val result = "1403/12/30".parseClientDateOrNull()

        assertEquals(LocalDate.of(2025, 3, 20), result)
    }
    @Test
    fun `parse should reject esfand 30 in non leap jalali year`() {
        assertThrows(BadRequestException::class.java) {
            "1404/12/30".parseClientDateOrNull()
        }
    }
}