package com.espresso.api.report

import com.espresso.api.customer.Customer
import java.time.LocalDateTime
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository

interface ReportRepository : Repository<Customer, Long> {
    @Query(
        value = """
            WITH totals AS (
                SELECT
                    s.customer_id,
                    SUM(s.total_orders) AS total_orders,
                    SUM(s.total_items) AS total_items,
                    SUM(s.total_spent) AS total_spent
                FROM customer_daily_summary s
                WHERE s.summary_date >= CAST(:startDate AS DATE)
                  AND s.summary_date < CAST(:endDate AS DATE)
                GROUP BY s.customer_id
            ),
            top_totals AS MATERIALIZED (
                SELECT
                    customer_id,
                    total_orders,
                    total_items,
                    total_spent
                FROM totals
                ORDER BY total_spent DESC, customer_id
                LIMIT 100
            )
            SELECT
                c.id AS "customerId",
                c.name AS "customerName",
                t.total_orders AS "totalOrders",
                t.total_items AS "totalItems",
                t.total_spent AS "totalSpent"
            FROM top_totals t
            JOIN customer c
                ON c.id = t.customer_id
            ORDER BY t.total_spent DESC, t.customer_id
        """,
        nativeQuery = true
    )
    fun findTopSpenders(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<CustomerTopSpenderProjection>
}
