package com.espresso.api.customer

import java.time.LocalDateTime

interface CustomerProjection {
    val id: Long
    val name: String
    val email: String
    val status: String
    val createdAt: LocalDateTime
}
