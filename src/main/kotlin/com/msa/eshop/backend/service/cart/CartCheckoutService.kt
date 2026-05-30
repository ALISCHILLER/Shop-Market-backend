package com.msa.eshop.backend.service.cart

import com.msa.eshop.backend.common.BadRequestException
import com.msa.eshop.backend.common.InsertCartModelRequest
import com.msa.eshop.backend.common.NotFoundException
import com.msa.eshop.backend.common.toUuidOrBadRequest
import com.msa.eshop.backend.domain.CartRepository
import com.msa.eshop.backend.domain.CustomerAddressRepository
import com.msa.eshop.backend.domain.PaymentTermRepository
import com.msa.eshop.backend.service.CurrentUserService
import com.msa.eshop.backend.service.PricingRequest
import com.msa.eshop.backend.service.PricingService
import com.msa.eshop.backend.service.catalog.ProductResolver
import com.msa.eshop.backend.service.pricing.PaymentKindResolver
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartCheckoutService(
    private val currentUserService: CurrentUserService,
    private val addressRepository: CustomerAddressRepository,
    private val paymentTermRepository: PaymentTermRepository,
    private val cartRepository: CartRepository,
    private val productResolver: ProductResolver,
    private val pricingService: PricingService,
    private val paymentKindResolver: PaymentKindResolver,
    private val cartCodeGenerator: CartCodeGenerator,
    private val cartLineNormalizer: CartLineNormalizer,
    private val cartAssembler: CartAssembler
) {
    @Transactional
    fun checkout(requests: List<InsertCartModelRequest>): Boolean {
        val header = cartLineNormalizer.extractCheckoutHeader(requests)
        val lines = cartLineNormalizer.normalizeCheckoutLines(requests)

        val currentCustomer = currentUserService.requireCustomer()

        val addressId = header.customerAddressId.toUuidOrBadRequest("شناسه آدرس معتبر نیست")
        val paymentTermId = header.paymentTermId.toUuidOrBadRequest("شناسه روش پرداخت معتبر نیست")

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

        val paymentKind = paymentKindResolver.resolve(paymentTerm)
        val productsByCode = productResolver.requireByCodes(lines.map { it.productCode })

        val pricingRequests = lines.map { line ->
            PricingRequest(
                product = productsByCode.getValue(line.productCode),
                quantity = line.quantity,
                paymentKind = paymentKind
            )
        }

        val priceLines = pricingService.calculateBatch(
            requests = pricingRequests,
            paymentTerm = paymentTerm,
            paymentKind = paymentKind
        )

        val cart = cartAssembler.assemble(
            cartCode = cartCodeGenerator.next(),
            customer = currentCustomer,
            address = address,
            paymentTerm = paymentTerm,
            priceLines = priceLines
        )

        cartRepository.save(cart)
        return true
    }
}