package com.example.sts.config

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class FirebaseAuthenticationFilter(
    private val firebaseAuth: FirebaseAuth
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        logger.info("Authorization Header: $authHeader")

        val token = authHeader?.substringAfter("Bearer ")

        if (token != null) {
            try {
                val decodedToken: FirebaseToken = firebaseAuth.verifyIdToken(token)
                val uid = decodedToken.uid
                val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
                val auth = UsernamePasswordAuthenticationToken(uid, null, authorities)
                SecurityContextHolder.getContext().authentication = auth
            } catch (e: Exception) {
                // Invalid token
                SecurityContextHolder.clearContext()
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.message)
                return
            }
        }

        filterChain.doFilter(request, response)
    }
}
