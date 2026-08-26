package com.espresso.api.report

import java.time.LocalDate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReportService(
    private val reportRepository: ReportRepository,
    private val topSpendersCache: TopSpendersCache
) {
    fun findTopSpenders(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CustomerTopSpenderProjection> {
        require(!startDate.isAfter(endDate)) {
            "startDate must be before or equal to endDate"
        }

        topSpendersCache.get(startDate, endDate)?.let { return it }

        val startedAt = System.nanoTime()
        val result = try {
            reportRepository.findTopSpenders(
                startDate = startDate.atStartOfDay(),
                endDate = endDate.plusDays(1).atStartOfDay()
            ).also { rows ->
                log.info(
                    "DB_TOP_SPENDERS_QUERY startDate={} endDate={} durationMs={} entries={} source=postgresql",
                    startDate,
                    endDate,
                    elapsedMilliseconds(startedAt),
                    rows.size
                )
            }
        } catch (exception: Exception) {
            log.error(
                "DB_TOP_SPENDERS_QUERY_ERROR startDate={} endDate={} durationMs={} source=postgresql exceptionType={}",
                startDate,
                endDate,
                elapsedMilliseconds(startedAt),
                exception::class.simpleName,
                exception
            )
            throw exception
        }

        topSpendersCache.put(startDate, endDate, result)

        return result
    }

    private fun elapsedMilliseconds(startedAt: Long): Long =
        (System.nanoTime() - startedAt) / 1_000_000

    private companion object {
        val log = LoggerFactory.getLogger(ReportService::class.java)
    }
}
