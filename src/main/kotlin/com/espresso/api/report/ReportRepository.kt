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
                COUNT(DISTINCT o.id) AS "totalOrders",
                SUM(oi.quantity) AS "totalItems",
                SUM(oi.quantity * oi.unit_price) AS "totalSpent"
            FROM customer c
            JOIN orders o
                ON o.customer_id = c.id
            JOIN order_item oi
                ON oi.order_id = o.id
            WHERE o.created_at >= :startDate
              AND o.created_at < :endDate
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
