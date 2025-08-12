package com.example.sts.controller

import com.example.sts.service.AuthService
import com.example.sts.service.model.auth.User
import com.google.firebase.auth.FirebaseAuth
import org.springframework.stereotype.Component

@Component
class ControllerUtils(
    private val authService: AuthService
) {
    fun getUserInfo(authHeader: String): User {
        val idToken = authHeader.removePrefix("Bearer ").trim()
        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken)

        return authService.getUserByFirebaseUid(decodedToken.uid)
    }
}