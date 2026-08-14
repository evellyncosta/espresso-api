package com.espresso.api.orderitem

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/order-items")
class OrderItemController(
    private val orderItemService: OrderItemService
) {
    @GetMapping
    fun findAll(pageable: Pageable): Page<OrderItemProjection> =
        orderItemService.findAll(pageable)
}
