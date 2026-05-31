package com.msa.eshop.backend.service

import com.msa.eshop.backend.domain.repository.CustomerRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SeedPasswordInitializer(
    private val customerRepository: CustomerRepository,
    private val passwordEncoder: PasswordEncoder
) : ApplicationRunner {
    @Transactional
    override fun run(args: ApplicationArguments?) {
        customerRepository.findAll()
            .filter { it.passwordHash.startsWith(PLAIN_PREFIX) }
            .forEach { customer ->
                val rawPassword = customer.passwordHash.removePrefix(PLAIN_PREFIX)
                customer.passwordHash = passwordEncoder.encode(rawPassword)
                customer.salt = "bcrypt"
            }
    }

    companion object {
        private const val PLAIN_PREFIX = "{plain}"
    }
}
