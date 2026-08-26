package com.espresso.api.logging

import java.util.UUID
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@Component
class RequestCorrelationFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestId = request.getHeader(REQUEST_ID_HEADER)
            ?.takeIf(::isValidRequestId)
            ?: UUID.randomUUID().toString()

        MDC.put(MDC_REQUEST_ID, requestId)
        response.setHeader(REQUEST_ID_HEADER, requestId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(MDC_REQUEST_ID)
        }
    }

    private fun isValidRequestId(requestId: String): Boolean =
        requestId.length in 1..128 && REQUEST_ID_PATTERN.matches(requestId)

    private companion object {
        const val REQUEST_ID_HEADER = "X-Request-Id"
        const val MDC_REQUEST_ID = "requestId"
        val REQUEST_ID_PATTERN = Regex("[A-Za-z0-9._-]+")
    }
}
