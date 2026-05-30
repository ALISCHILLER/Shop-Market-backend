package com.msa.eshop.backend.common

import java.math.BigDecimal
import java.math.RoundingMode

data class Money(
    val value: Long
) {
    init {
        if (value < 0) throw BadRequestException("مبلغ نمی‌تواند منفی باشد")
    }

    operator fun plus(other: Money): Money {
        val result = runCatching {
            Math.addExact(value, other.value)
        }.getOrElse {
            throw BadRequestException("مبلغ محاسبه‌شده بیش از حد مجاز است")
        }

        return Money(result)
    }

    operator fun minus(other: Money): Money {
        val result = value - other.value
        if (result < 0) throw BadRequestException("نتیجه محاسبه مبلغ منفی شد")
        return Money(result)
    }

    fun percent(percent: Int): Money {
        percent.requirePercent()

        if (percent == 0) return zero()

        val calculated = BigDecimal.valueOf(value)
            .multiply(BigDecimal.valueOf(percent.toLong()))
            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
            .toLong()

        return Money(calculated)
    }

    fun toPersistedInt(): Int {
        if (value > Int.MAX_VALUE) {
            throw BadRequestException("مبلغ سفارش بیش از حد مجاز است")
        }

        return value.toInt()
    }

    companion object {
        fun zero(): Money = Money(0)

        fun of(value: Int): Money {
            if (value < 0) throw BadRequestException("مبلغ نمی‌تواند منفی باشد")
            return Money(value.toLong())
        }

        fun multiply(unitPrice: Int, quantity: Int): Money {
            if (unitPrice < 0) throw BadRequestException("قیمت کالا معتبر نیست")
            if (quantity <= 0) throw BadRequestException("تعداد کالا باید بزرگ‌تر از صفر باشد")

            val result = runCatching {
                Math.multiplyExact(unitPrice.toLong(), quantity.toLong())
            }.getOrElse {
                throw BadRequestException("مبلغ محاسبه‌شده بیش از حد مجاز است")
            }

            return Money(result)
        }
    }
}