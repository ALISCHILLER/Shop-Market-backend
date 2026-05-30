package com.msa.eshop.backend.common

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

fun String.toUuidOrBadRequest(message: String = "شناسه معتبر نیست"): UUID =
    runCatching { UUID.fromString(trim()) }
        .getOrElse { throw BadRequestException(message) }

fun String?.cleanOrNull(): String? =
    this?.trim()?.takeIf { it.isNotBlank() }

fun String?.cleanRequired(message: String): String =
    this?.trim()?.takeIf { it.isNotBlank() }
        ?: throw BadRequestException(message)

fun Int.requireMin(min: Int, message: String): Int {
    if (this < min) throw BadRequestException(message)
    return this
}

fun Int.requirePercent(message: String = "درصد معتبر نیست"): Int {
    if (this !in 0..100) throw BadRequestException(message)
    return this
}

fun Double.requireLatitude(): Double {
    if (this !in -90.0..90.0) throw BadRequestException("عرض جغرافیایی معتبر نیست")
    return this
}

fun Double.requireLongitude(): Double {
    if (this !in -180.0..180.0) throw BadRequestException("طول جغرافیایی معتبر نیست")
    return this
}

fun validateGeoPair(latitude: Double?, longitude: Double?) {
    if ((latitude == null) != (longitude == null)) {
        throw BadRequestException("عرض و طول جغرافیایی باید با هم ارسال شوند")
    }

    latitude?.requireLatitude()
    longitude?.requireLongitude()
}

fun String?.parseClientDateOrNull(): LocalDate? {
    val value = this?.trim()?.normalizeDigits().orEmpty()
    if (value.isBlank()) return null

    val normalized = value
        .replace('/', '-')
        .replace('.', '-')

    val parts = normalized.split("-")
    if (parts.size == 3) {
        val year = parts[0].toIntOrNull()
        val month = parts[1].toIntOrNull()
        val day = parts[2].toIntOrNull()

        if (year != null && month != null && day != null) {
            if (year in 1200..1700) {
                return PersianDateConverter.toGregorian(year, month, day)
            }
        }
    }

    val patterns = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("yyyy-M-d"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    )

    return patterns.firstNotNullOfOrNull { formatter ->
        runCatching { LocalDate.parse(normalized, formatter) }.getOrNull()
    }
}

fun String.normalizeDigits(): String =
    map {
        when (it) {
            '۰' -> '0'
            '۱' -> '1'
            '۲' -> '2'
            '۳' -> '3'
            '۴' -> '4'
            '۵' -> '5'
            '۶' -> '6'
            '۷' -> '7'
            '۸' -> '8'
            '۹' -> '9'
            '٠' -> '0'
            '١' -> '1'
            '٢' -> '2'
            '٣' -> '3'
            '٤' -> '4'
            '٥' -> '5'
            '٦' -> '6'
            '٧' -> '7'
            '٨' -> '8'
            '٩' -> '9'
            else -> it
        }
    }.joinToString("")

object PersianDateConverter {
    private val jalaliMonthDays = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

    fun toGregorian(jy: Int, jm: Int, jd: Int): LocalDate {
        if (jm !in 1..12) throw BadRequestException("ماه تاریخ معتبر نیست")
        if (jd !in 1..31) throw BadRequestException("روز تاریخ معتبر نیست")

        val jy2 = jy - 979
        val jm2 = jm - 1
        val jd2 = jd - 1

        var jDayNo = 365 * jy2 + (jy2 / 33) * 8 + ((jy2 % 33 + 3) / 4)

        for (i in 0 until jm2) {
            jDayNo += jalaliMonthDays[i]
        }

        jDayNo += jd2

        var gDayNo = jDayNo + 79

        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true

        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524

            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }

        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }

        val gregorianMonthDays = intArrayOf(
            31,
            if (leap) 29 else 28,
            31,
            30,
            31,
            30,
            31,
            31,
            30,
            31,
            30,
            31
        )

        var gm = 0
        while (gm < 12 && gDayNo >= gregorianMonthDays[gm]) {
            gDayNo -= gregorianMonthDays[gm]
            gm++
        }

        return LocalDate.of(gy, gm + 1, gDayNo + 1)
    }
}