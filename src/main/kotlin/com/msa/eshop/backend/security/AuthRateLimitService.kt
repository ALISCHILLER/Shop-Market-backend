package com.msa.eshop.backend.security

import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Service
class AuthRateLimitService {
    private val clock: Clock = Clock.systemUTC()
    private val buckets = ConcurrentHashMap<String, Bucket>()
    private val requestCounter = AtomicLong(0)

    fun tryConsume(
        key: String,
        capacity: Int,
        window: Duration
    ): RateLimitDecision {
        require(capacity > 0) { "Rate limit capacity must be greater than zero" }
        require(!window.isZero && !window.isNegative) { "Rate limit window must be positive" }

        cleanupOccasionally(window)

        val now = clock.instant()
        val bucket = buckets.computeIfAbsent(key) {
            Bucket(
                remaining = capacity,
                windowStartedAt = now,
                lastSeenAt = now
            )
        }

        synchronized(bucket) {
            val elapsed = Duration.between(bucket.windowStartedAt, now)

            if (!elapsed.isNegative && elapsed >= window) {
                bucket.remaining = capacity
                bucket.windowStartedAt = now
            }

            bucket.lastSeenAt = now

            if (bucket.remaining <= 0) {
                val retryAfter = window.minus(Duration.between(bucket.windowStartedAt, now))
                    .seconds
                    .coerceAtLeast(1)

                return RateLimitDecision(
                    allowed = false,
                    retryAfterSeconds = retryAfter
                )
            }

            bucket.remaining -= 1

            return RateLimitDecision(
                allowed = true,
                retryAfterSeconds = 0
            )
        }
    }

    private fun cleanupOccasionally(window: Duration) {
        val count = requestCounter.incrementAndGet()
        if (count % CLEANUP_EVERY_REQUESTS != 0L) return

        val now = clock.instant()
        val ttl = window.multipliedBy(CLEANUP_WINDOW_MULTIPLIER)

        buckets.entries.removeIf { (_, bucket) ->
            Duration.between(bucket.lastSeenAt, now) > ttl
        }
    }

    private data class Bucket(
        var remaining: Int,
        var windowStartedAt: Instant,
        var lastSeenAt: Instant
    )

    private companion object {
        const val CLEANUP_EVERY_REQUESTS = 500L
        const val CLEANUP_WINDOW_MULTIPLIER = 4L
    }
}

data class RateLimitDecision(
    val allowed: Boolean,
    val retryAfterSeconds: Long
)