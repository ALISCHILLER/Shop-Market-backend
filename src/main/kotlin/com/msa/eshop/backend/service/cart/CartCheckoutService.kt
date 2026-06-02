package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.dtos.CartCheckoutRequest
import com.msa.eshop.backend.common.dtos.CartCheckoutResponse
import com.msa.eshop.backend.domain.repository.CartRepository
import com.msa.eshop.backend.domain.repository.CustomerAddressRepository
import com.msa.eshop.backend.service.CurrentUserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartCheckoutService(
    private val currentUserService: CurrentUserService,
    private val addressRepository: CustomerAddressRepository,
    private val cartRepository: CartRepository,
    private val cartCodeGenerator: CartCodeGenerator,
    private val cartLineNormalizer: CartLineNormalizer,
    private val cartPricingCalculator: CartPricingCalculator,
    private val cartAssembler: CartAssembler
) {

    @Transactional
    fun checkout(request: CartCheckoutRequest): CartCheckoutResponse {
        val currentCustomer = currentUserService.requireCustomer()

        val address = addressRepository.findById(request.customerAddressId)
            .orElseThrow { NotFoundException("آدرس سفارش پیدا نشد") }

        if (address.customer?.id != currentCustomer.id) {
            throw BadRequestException("آدرس انتخاب‌شده متعلق به این مشتری نیست")
        }

        val lines = cartLineNormalizer.normalize(request.items)

        val pricingResult = cartPricingCalculator.calculate(
            CartPricingRequest(
                paymentTermId = request.paymentTermId,
                lines = lines
            )
        )

        val cart = cartAssembler.assemble(
            cartCode = cartCodeGenerator.next(),
            customer = currentCustomer,
            address = address,
            pricingResult = pricingResult
        )

        val saved = cartRepository.save(cart)

        return CartCheckoutResponse(
            cartId = requireNotNull(saved.id),
            cartCode = saved.cartCode,
            statusCode = saved.statusCode,
            statusName = saved.statusName,
            subtotal = saved.subtotal,
            discountTotal = saved.discountTotal,
            taxTotal = saved.taxTotal,
            total = saved.total
        )
    }
}