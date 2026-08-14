package com.espresso.api.report

import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/reports")
class ReportController(
    private val reportService: ReportService
) {
    @GetMapping("/customers/top-spenders")
    @ResponseStatus(HttpStatus.OK)
    fun findTopSpenders(
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDate: LocalDate,
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate
    ): List<CustomerTopSpenderProjection> {
        if (startDate.isAfter(endDate)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "startDate must be before or equal to endDate"
            )
        }

        return reportService.findTopSpenders(startDate, endDate)
    }
}
