package com.espresso.api.report

import java.math.BigDecimal

data class TopSpenderCacheEntry(
    override val customerId: Long,
    override val customerName: String,
    override val totalOrders: Long,
    override val totalItems: Long,
    override val totalSpent: BigDecimal
) : CustomerTopSpenderProjection
