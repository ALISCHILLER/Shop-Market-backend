package com.msa.eshop.backend.api

import com.msa.eshop.backend.common.OrderAddressResultModel
import com.msa.eshop.backend.service.CartService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/User")
class CustomerAddressController(
    private val cartService: CartService
) {
    @GetMapping("/GetCustomerAddress")
    fun customerAddresses(): OrderAddressResultModel =
        OrderAddressResultModel(cartService.currentCustomerAddresses())
}