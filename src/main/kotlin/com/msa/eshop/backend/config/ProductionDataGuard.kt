package com.msa.eshop.backend.config

import com.msa.eshop.backend.domain.repository.CustomerRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class ProductionDataGuard(
    private val environment: Environment,
    private val customerRepository: CustomerRepository
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        if (!isStrictRuntimeProfile()) return

        val unsafeCustomers = customerRepository.findAll()
            .filter { customer ->
                customer.passwordHash.startsWith(PLAIN_PASSWORD_PREFIX) ||
                        customer.salt.equals(DEV_SEED_SALT, ignoreCase = true)
            }
            .map { it.customerCode }

        if (unsafeCustomers.isNotEmpty()) {
            error(
                "Unsafe development seed credentials were detected outside dev/test profiles. " +
                        "Remove or replace seeded credentials before running this environment. " +
                        "Affected customer codes: ${unsafeCustomers.joinToString(", ")}"
            )
        }
    }

    private fun isStrictRuntimeProfile(): Boolean {
        val activeProfiles = environment.activeProfiles.map { it.lowercase() }.toSet()
        if (activeProfiles.isEmpty()) return false

        return activeProfiles.none { it in RELAXED_PROFILES }
    }

    private companion object {
        const val PLAIN_PASSWORD_PREFIX = "{plain}"
        const val DEV_SEED_SALT = "dev-seed"
        val RELAXED_PROFILES = setOf("dev", "test", "local")
    }
}