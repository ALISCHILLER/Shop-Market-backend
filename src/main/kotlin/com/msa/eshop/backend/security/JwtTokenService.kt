package com.msa.eshop.backend.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.eshop.backend.config.JwtProperties
import com.msa.eshop.backend.domain.Customer
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class JwtTokenService(
    private val jwtProperties: JwtProperties,
    private val objectMapper: ObjectMapper
) {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun generateToken(customer: Customer): String {
        val now = Instant.now()
        val header = mapOf("alg" to "HS256", "typ" to "JWT")
        val payload = mapOf(
            "sub" to customer.customerCode,
            "uid" to customer.id.toString(),
            "role" to customer.role,
            "iat" to now.epochSecond,
            "exp" to now.plusSeconds(jwtProperties.expirationMinutes * 60).epochSecond
        )

        val headerPart = encodeJson(header)
        val payloadPart = encodeJson(payload)
        val signingInput = "$headerPart.$payloadPart"
        val signature = sign(signingInput)
        return "$signingInput.$signature"
    }

    fun parseToken(token: String): JwtClaims? {
        val parts = token.split('.')
        if (parts.size != 3) return null

        val signingInput = parts[0] + "." + parts[1]
        val expectedSignature = sign(signingInput)
        if (!MessageDigest.isEqual(expectedSignature.toByteArray(), parts[2].toByteArray())) {
            return null
        }

        val payloadBytes = decoder.decode(parts[1])
        val payload = objectMapper.readValue(payloadBytes, Map::class.java)
        val exp = (payload["exp"] as? Number)?.toLong() ?: return null
        if (Instant.now().epochSecond >= exp) return null

        val subject = payload["sub"]?.toString() ?: return null
        val userId = payload["uid"]?.toString() ?: return null
        val role = payload["role"]?.toString() ?: "CUSTOMER"
        return JwtClaims(subject = subject, userId = userId, role = role)
    }

    private fun encodeJson(value: Any): String =
        encoder.encodeToString(objectMapper.writeValueAsBytes(value))

    private fun sign(input: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(jwtProperties.secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return encoder.encodeToString(mac.doFinal(input.toByteArray(Charsets.UTF_8)))
    }
}

data class JwtClaims(
    val subject: String,
    val userId: String,
    val role: String
)
