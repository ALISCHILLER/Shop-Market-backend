package com.msa.eshop.backend.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.eshop.backend.config.JwtProperties
import com.msa.eshop.backend.domain.entity.Customer
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

        val header = JwtHeader(
            alg = JWT_ALGORITHM,
            typ = JWT_TYPE
        )

        val payload = JwtPayload(
            sub = customer.customerCode,
            uid = requireNotNull(customer.id).toString(),
            role = customer.role.uppercase(),
            iat = now.epochSecond,
            exp = now.plusSeconds(jwtProperties.expirationMinutes * 60).epochSecond
        )

        val headerPart = encodeJson(header)
        val payloadPart = encodeJson(payload)
        val signingInput = "$headerPart.$payloadPart"
        val signature = sign(signingInput)

        return "$signingInput.$signature"
    }

    fun parseToken(token: String): JwtClaims? {
        return runCatching {
            val parts = token.split('.')
            if (parts.size != 3) return null

            val headerBytes = decoder.decode(parts[0])
            val header = objectMapper.readValue(headerBytes, JwtHeader::class.java)

            if (header.alg != JWT_ALGORITHM || header.typ != JWT_TYPE) {
                return null
            }

            val signingInput = "${parts[0]}.${parts[1]}"

            val expectedSignatureBytes = signToBytes(signingInput)
            val actualSignatureBytes = decoder.decode(parts[2])

            if (!MessageDigest.isEqual(expectedSignatureBytes, actualSignatureBytes)) {
                return null
            }

            val payloadBytes = decoder.decode(parts[1])
            val payload = objectMapper.readValue(payloadBytes, JwtPayload::class.java)

            if (Instant.now().epochSecond >= payload.exp) return null

            val subject = payload.sub.takeIf { it.isNotBlank() } ?: return null
            val userId = payload.uid.takeIf { it.isNotBlank() } ?: return null
            val role = payload.role.uppercase().removePrefix("ROLE_").ifBlank { "CUSTOMER" }

            JwtClaims(
                subject = subject,
                userId = userId,
                role = role
            )
        }.getOrNull()
    }

    private fun encodeJson(value: Any): String =
        encoder.encodeToString(objectMapper.writeValueAsBytes(value))

    private fun sign(input: String): String =
        encoder.encodeToString(signToBytes(input))

    private fun signToBytes(input: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val key = SecretKeySpec(jwtProperties.secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(key)

        return mac.doFinal(input.toByteArray(Charsets.UTF_8))
    }

    private companion object {
        const val JWT_ALGORITHM = "HS256"
        const val JWT_TYPE = "JWT"
    }
}

data class JwtClaims(
    val subject: String,
    val userId: String,
    val role: String
)
data class JwtHeader(
    val alg: String = "",
    val typ: String = ""
)

private data class JwtPayload(
    val sub: String = "",
    val uid: String = "",
    val role: String = "CUSTOMER",
    val iat: Long = 0,
    val exp: Long = 0
)