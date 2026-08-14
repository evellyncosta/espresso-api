package com.espresso.api.customer

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springdoc.core.annotations.ParameterObject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/customers")
class CustomerController(
    private val customerService: CustomerService
) {
    @GetMapping
    fun findAll(@ParameterObject pageable: Pageable): Page<CustomerProjection> =
        customerService.findAll(pageable)
}
