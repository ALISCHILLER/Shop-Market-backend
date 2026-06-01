package com.msa.eshop.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.rate-limit.auth")
class AuthRateLimitProperties {
    var enabled: Boolean = true
    var login: Rule = Rule(capacity = 10, windowSeconds = 60)
    var refresh: Rule = Rule(capacity = 30, windowSeconds = 60)
    var logout: Rule = Rule(capacity = 60, windowSeconds = 60)
    var changePassword: Rule = Rule(capacity = 5, windowSeconds = 300)

    class Rule(
        var capacity: Int = 10,
        var windowSeconds: Long = 60
    )
}