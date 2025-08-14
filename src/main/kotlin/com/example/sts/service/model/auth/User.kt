package com.example.sts.service.model.auth

import java.time.Instant

data class User(
    val userId: Long,
    val userName: String,
    val userEmail: String,
    val createdAt: String,
    val youTubeAccessToken: String?,
    val refreshToken: String?,
    val expiresAt: Instant?,
)
