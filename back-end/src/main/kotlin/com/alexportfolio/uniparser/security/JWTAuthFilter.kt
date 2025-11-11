package com.alexportfolio.uniparser.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class JWTAuthFilter(private val tokenProcessor: TokenProcessor): OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization") ?: ""
        val tokenFromProtocol = request.getHeader("Sec-WebSocket-Protocol")
        val token = extractBearerToken(authHeader) ?: tokenFromProtocol

        val auth = tokenProcessor.getAuthObjFromToken(token)
        if(auth!=null) {
            SecurityContextHolder.getContext().setAuthentication(auth)
            tokenFromProtocol?.let { response.addHeader("Sec-WebSocket-Protocol", it) }
        }
        filterChain.doFilter(request,response)
    }
    private fun extractBearerToken(header: String): String? {
        val prefix = "Bearer "
        return if (header.length >= prefix.length && header.regionMatches(0, prefix, 0, prefix.length, ignoreCase = true)) {
            header.substring(prefix.length).trim()
        } else null
    }
}