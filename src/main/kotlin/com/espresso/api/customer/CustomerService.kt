package com.espresso.api.customer

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class CustomerService(
    private val customerRepository: CustomerRepository
) {
    fun findAll(pageable: Pageable): Page<CustomerProjection> =
        customerRepository.findAllProjectedBy(pageable)
}
