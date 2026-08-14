package com.espresso.api.orderitem

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OrderItemRepository : JpaRepository<OrderItem, Long> {
    @Query(
        """
        select
            oi.id as id,
            o.id as orderId,
            p.id as productId,
            oi.quantity as quantity,
            oi.unitPrice as unitPrice
        from OrderItem oi
        join oi.order o
        join oi.product p
        """
    )
    fun findAllProjected(pageable: Pageable): Page<OrderItemProjection>
}
