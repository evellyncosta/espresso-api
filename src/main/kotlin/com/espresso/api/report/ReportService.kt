package com.espresso.api.report

import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
class ReportService(
    private val reportRepository: ReportRepository
) {
    fun findTopSpenders(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CustomerTopSpenderProjection> {
        require(!startDate.isAfter(endDate)) {
            "startDate must be before or equal to endDate"
        }

        return reportRepository.findTopSpenders(
            startDate = startDate.atStartOfDay(),
            endDate = endDate.plusDays(1).atStartOfDay()
        )
    }
}
