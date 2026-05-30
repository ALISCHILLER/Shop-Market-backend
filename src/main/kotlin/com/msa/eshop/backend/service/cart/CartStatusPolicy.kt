package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.domain.CartStatus
import org.springframework.stereotype.Component

@Component
class CartStatusPolicy {
    fun assertCanChange(
        current: CartStatus,
        target: CartStatus
    ) {
        if (current == target) return

        when (current) {
            CartStatus.CANCELLED -> {
                throw BadRequestException("سفارش لغو شده قابل تغییر وضعیت نیست")
            }

            CartStatus.DELIVERED -> {
                throw BadRequestException("سفارش تحویل شده قابل تغییر وضعیت نیست")
            }

            CartStatus.REGISTERED -> {
                if (target == CartStatus.DELIVERED) {
                    throw BadRequestException("سفارش ثبت شده باید ابتدا وارد مرحله بررسی شود")
                }
            }

            CartStatus.PROCESSING -> {
                if (target == CartStatus.REGISTERED) {
                    throw BadRequestException("سفارش در حال بررسی نمی‌تواند به وضعیت ثبت شده برگردد")
                }
            }
        }
    }
}