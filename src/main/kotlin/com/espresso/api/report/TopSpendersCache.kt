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
            val value = redisTemplate.opsForValue().get(key(startDate, endDate))
                ?: return null

            objectMapper
                .readValue(value, Array<TopSpenderCacheEntry>::class.java)
                .toList()
        } catch (exception: Exception) {
            log.warn("Unable to read Top Spenders cache", exception)
            null
        }
    }

    fun put(
        startDate: LocalDate,
        endDate: LocalDate,
        value: List<CustomerTopSpenderProjection>
    ) {
        try {
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
                key(startDate, endDate),
                objectMapper.writeValueAsString(entries),
                TTL
            )
        } catch (exception: Exception) {
            log.warn("Unable to write Top Spenders cache", exception)
        }
    }

    private fun key(startDate: LocalDate, endDate: LocalDate): String =
        "top-spenders:$startDate:$endDate"

    private companion object {
        val log = LoggerFactory.getLogger(TopSpendersCache::class.java)
        val TTL: Duration = Duration.ofDays(1)
    }
}
