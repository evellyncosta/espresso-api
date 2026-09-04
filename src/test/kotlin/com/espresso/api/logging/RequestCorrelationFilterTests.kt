package com.espresso.api.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class RequestCorrelationFilterTests {

    private val filter = RequestCorrelationFilter()

    @Test
    fun `returns the valid request id received from the client`() {
        val request = MockHttpServletRequest().apply {
            addHeader("X-Request-Id", "request-123")
        }
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, MockFilterChain())

        assertEquals("request-123", response.getHeader("X-Request-Id"))
    }
}
