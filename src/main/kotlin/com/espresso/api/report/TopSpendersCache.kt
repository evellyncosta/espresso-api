package com.espresso.api.report

import java.time.Duration
import java.time.LocalDate
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class TopSpendersCache(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {
    fun get(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CustomerTopSpenderProjection>? {
        return try {
            val cacheKey = key(startDate, endDate)
            val value = redisTemplate.opsForValue().get(cacheKey)
            if (value == null) {
                log.info(
                    "CACHE_MISS cacheKey={} startDate={} endDate={}",
                    cacheKey,
                    startDate,
                    endDate
                )
                return null
            }

            val entries = objectMapper
                .readValue(value, Array<TopSpenderCacheEntry>::class.java)
                .toList()

            log.info(
                "CACHE_HIT cacheKey={} startDate={} endDate={} entries={}",
                cacheKey,
                startDate,
                endDate,
                entries.size
            )
            entries
        } catch (exception: Exception) {
            log.warn(
                "CACHE_READ_ERROR cacheKey={} exceptionType={}",
                key(startDate, endDate),
                exception::class.simpleName,
                exception
            )
            null
        }
    }

    fun put(
        startDate: LocalDate,
        endDate: LocalDate,
        value: List<CustomerTopSpenderProjection>
    ) {
        try {
            val cacheKey = key(startDate, endDate)
            val entries = value.map { projection ->
                TopSpenderCacheEntry(
                    customerId = projection.customerId,
                    customerName = projection.customerName,
                    totalOrders = projection.totalOrders,
                    totalItems = projection.totalItems,
                    totalSpent = projection.totalSpent
                )
            }

            redisTemplate.opsForValue().set(
                cacheKey,
                objectMapper.writeValueAsString(entries),
                TTL
            )

            log.info(
                "CACHE_WRITE_SUCCESS cacheKey={} startDate={} endDate={} entries={} ttlSeconds={}",
                cacheKey,
                startDate,
                endDate,
                entries.size,
                TTL_SECONDS
            )
        } catch (exception: Exception) {
            log.warn(
                "CACHE_WRITE_ERROR cacheKey={} exceptionType={}",
                key(startDate, endDate),
                exception::class.simpleName,
                exception
            )
        }
    }

    private fun key(startDate: LocalDate, endDate: LocalDate): String =
        "top-spenders:$startDate:$endDate"

    private companion object {
        val log = LoggerFactory.getLogger(TopSpendersCache::class.java)
        val TTL: Duration = Duration.ofDays(1)
        const val TTL_SECONDS: Long = 86_400L
    }
}
