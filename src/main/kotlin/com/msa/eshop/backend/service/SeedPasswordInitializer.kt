package com.msa.eshop.backend.service

import com.msa.eshop.backend.domain.repository.CustomerRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("dev", "test", "local")
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
                customer.salt = PASSWORD_ALGORITHM
                customer.passwordChangeRequired = true
            }
    }

    private companion object {
        const val PLAIN_PREFIX = "{plain}"
        const val PASSWORD_ALGORITHM = "bcrypt"
    }
}