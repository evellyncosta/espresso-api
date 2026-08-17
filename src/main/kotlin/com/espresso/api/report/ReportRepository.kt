package com.espresso.api.report

import com.espresso.api.customer.Customer
import java.time.LocalDateTime
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository

interface ReportRepository : Repository<Customer, Long> {
    @Query(
        value = """
            SELECT
                c.id AS "customerId",
                c.name AS "customerName",
                SUM(s.total_orders) AS "totalOrders",
                SUM(s.total_items) AS "totalItems",
                SUM(s.total_spent) AS "totalSpent"
            FROM customer c
            JOIN customer_daily_summary s
                ON s.customer_id = c.id
            WHERE s.summary_date >= CAST(:startDate AS DATE)
              AND s.summary_date < CAST(:endDate AS DATE)
            GROUP BY c.id, c.name
            ORDER BY "totalSpent" DESC
            LIMIT 100
        """,
        nativeQuery = true
    )
    fun findTopSpenders(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<CustomerTopSpenderProjection>
}
