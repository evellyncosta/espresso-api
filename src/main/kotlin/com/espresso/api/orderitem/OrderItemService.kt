package com.espresso.api.orderitem

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class OrderItemService(
    private val orderItemRepository: OrderItemRepository
) {
    fun findAll(pageable: Pageable): Page<OrderItemProjection> =
        orderItemRepository.findAllProjected(pageable)
}
