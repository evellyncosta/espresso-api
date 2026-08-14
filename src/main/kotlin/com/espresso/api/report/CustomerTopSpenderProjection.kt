package com.espresso.api.report

import java.math.BigDecimal

interface CustomerTopSpenderProjection {
    val customerId: Long
    val customerName: String
    val totalOrders: Long
    val totalItems: Long
    val totalSpent: BigDecimal
}
