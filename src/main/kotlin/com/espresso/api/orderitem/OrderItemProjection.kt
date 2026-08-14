package com.espresso.api.orderitem

import java.math.BigDecimal

interface OrderItemProjection {
    val id: Long
    val orderId: Long
    val productId: Long
    val quantity: Int
    val unitPrice: BigDecimal
}
