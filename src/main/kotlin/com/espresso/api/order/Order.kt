package com.espresso.api.order

import com.espresso.api.customer.Customer
import jakarta.persistence.Entity
import jakarta.persistence.Column
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    var customer: Customer? = null,

    var status: String = "",

    @Column(name = "total_amount")
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.MIN,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.MIN
)
