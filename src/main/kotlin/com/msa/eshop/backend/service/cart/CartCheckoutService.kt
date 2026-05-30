package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.InsertCartModelRequest
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.toUuidOrBadRequest
import com.msa.eshop.backend.domain.Cart
import com.msa.eshop.backend.domain.CartItem
import com.msa.eshop.backend.domain.CartRepository
import com.msa.eshop.backend.domain.CartStatus
import com.msa.eshop.backend.domain.CustomerAddressRepository
import com.msa.eshop.backend.domain.PaymentTermRepository
import com.msa.eshop.backend.domain.Product
import com.msa.eshop.backend.domain.ProductRepository
import com.msa.eshop.backend.service.CurrentUserService
import com.msa.eshop.backend.service.PricingService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class CartCheckoutService(
    private val currentUserService: CurrentUserService,
    private val addressRepository: CustomerAddressRepository,
    private val paymentTermRepository: PaymentTermRepository,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val pricingService: PricingService,
    private val cartCodeGenerator: CartCodeGenerator,
    private val cartLineNormalizer: CartLineNormalizer
) {
    @Transactional
    fun checkout(requests: List<InsertCartModelRequest>): Boolean {
        val lines = cartLineNormalizer.normalizeCheckoutLines(requests)

        val currentCustomer = currentUserService.requireCustomer()
        val first = requests.first()

        val addressId = first.customerAddressId.toUuidOrBadRequest("شناسه آدرس معتبر نیست")
        val paymentTermId = first.paymentTermId.toUuidOrBadRequest("شناسه روش پرداخت معتبر نیست")

        val address = addressRepository.findById(addressId)
            .orElseThrow { NotFoundException("آدرس سفارش پیدا نشد") }

        if (address.customer?.id != currentCustomer.id) {
            throw BadRequestException("آدرس انتخاب‌شده متعلق به این مشتری نیست")
        }

        val paymentTerm = paymentTermRepository.findById(paymentTermId)
            .orElseThrow { NotFoundException("روش پرداخت پیدا نشد") }

        if (!paymentTerm.active) {
            throw BadRequestException("روش پرداخت انتخاب‌شده غیرفعال است")
        }

        val products = loadProducts(lines.map { it.productCode })
        val status = CartStatus.REGISTERED

        val cart = Cart(
            cartCode = cartCodeGenerator.next(),
            customer = currentCustomer,
            address = address,
            paymentTerm = paymentTerm,
            customerNameSnapshot = currentCustomer.customerName,
            customerAddressSnapshot = address.customerAddress,
            statusName = status.title,
            statusColor = status.color,
            salesDate = LocalDate.now()
        )

        lines.forEach { line ->
            val product = products[line.productCode]
                ?: throw NotFoundException("کالا با کد ${line.productCode} پیدا نشد")

            val priceLine = pricingService.calculate(
                product = product,
                quantity = line.quantity,
                paymentTerm = paymentTerm
            )

            val cartItem = CartItem(
                product = product,
                productCode = product.productCode,
                productName = product.productName.orEmpty(),
                productImageUrl = product.productImage,
                quantity = line.quantity,
                price = product.price,
                discount = priceLine.totalDiscount.toPersistedInt(),
                tax = priceLine.tax.toPersistedInt(),
                total = priceLine.total.toPersistedInt()
            )

            cart.addItem(cartItem)

            cart.subtotal = safePlus(cart.subtotal, priceLine.gross.toPersistedInt(), "جمع مبلغ سفارش بیش از حد مجاز است")
            cart.discountTotal = safePlus(cart.discountTotal, priceLine.totalDiscount.toPersistedInt(), "جمع تخفیف سفارش بیش از حد مجاز است")
            cart.taxTotal = safePlus(cart.taxTotal, priceLine.tax.toPersistedInt(), "جمع مالیات سفارش بیش از حد مجاز است")
            cart.total = safePlus(cart.total, priceLine.total.toPersistedInt(), "جمع نهایی سفارش بیش از حد مجاز است")
        }

        cartRepository.save(cart)
        return true
    }

    private fun loadProducts(productCodes: List<Int>): Map<Int, Product> {
        val uniqueCodes = productCodes.toSet()

        val products = productRepository.findByProductCodeIn(uniqueCodes)
            .associateBy { it.productCode }

        val missingCodes = uniqueCodes - products.keys
        if (missingCodes.isNotEmpty()) {
            throw NotFoundException("کالا با کد ${missingCodes.first()} پیدا نشد")
        }

        return products
    }

    private fun safePlus(current: Int, value: Int, message: String): Int {
        val result = current.toLong() + value.toLong()

        if (result > Int.MAX_VALUE) {
            throw BadRequestException(message)
        }

        return result.toInt()
    }
}