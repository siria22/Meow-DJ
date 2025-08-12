package com.example.sts.controller.auth

// auth
data class AuthProfileResponse(
    val uid: Long,
    val name: String,
    val email: String,
    val createdAt: String
)

// youtube-connect
data class AuthYouTubeConnectRequest(
    val authCode: String
)

data class AuthYouTubeConnectResponse(
    val isSuccess: Boolean,
)
