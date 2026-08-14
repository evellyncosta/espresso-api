package com.espresso.api.product

import jakarta.persistence.Entity
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "product")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String = "",

    var price: BigDecimal = BigDecimal.ZERO,

    var status: String = "",

    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.MIN
)
