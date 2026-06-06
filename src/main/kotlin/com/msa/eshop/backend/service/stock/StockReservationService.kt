package com.msa.eshop.backend.service.stock

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.domain.entity.Cart
import com.msa.eshop.backend.domain.entity.StockReservation
import com.msa.eshop.backend.domain.entity.StockReservationStatus
import com.msa.eshop.backend.domain.repository.ProductRepository
import com.msa.eshop.backend.domain.repository.StockReservationRepository
import com.msa.eshop.backend.service.cart.NormalizedCartLine
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StockReservationService(
    private val productRepository: ProductRepository,
    private val reservationRepository: StockReservationRepository
) {

    @Transactional
    fun reserveForCart(
        cart: Cart,
        lines: List<NormalizedCartLine>
    ) {
        if (lines.isEmpty()) {
            throw BadRequestException("سبد خرید خالی است")
        }

        val products = productRepository.findByProductCodeInForUpdate(
            lines.map { it.productCode }.toSet()
        ).associateBy { it.productCode }

        lines.forEach { line ->
            val product = products[line.productCode]
                ?: throw BadRequestException("کالا با کد ${line.productCode} پیدا نشد")

            if (product.availableStock() < line.quantity) {
                throw BadRequestException("موجودی کالا ${product.productName ?: product.productCode} کافی نیست")
            }

            product.reserveStock(line.quantity)

            reservationRepository.save(
                StockReservation(
                    cart = cart,
                    product = product,
                    productCode = product.productCode,
                    quantity = line.quantity
                )
            )
        }
    }

    @Transactional
    fun releaseForCart(cart: Cart) {
        val cartId = requireNotNull(cart.id)

        val reservations = reservationRepository.findByCartIdAndStatus(
            cartId = cartId,
            status = StockReservationStatus.ACTIVE.name
        )

        reservations.forEach { reservation ->
            reservation.product.releaseReservedStock(reservation.quantity)
            reservation.release()
        }
    }

    @Transactional
    fun consumeForCart(cart: Cart) {
        val cartId = requireNotNull(cart.id)

        val reservations = reservationRepository.findByCartIdAndStatus(
            cartId = cartId,
            status = StockReservationStatus.ACTIVE.name
        )

        reservations.forEach { reservation ->
            reservation.product.consumeReservedStock(reservation.quantity)
            reservation.consume()
        }
    }
}