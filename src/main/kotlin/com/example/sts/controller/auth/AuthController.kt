package com.example.sts.controller.auth

import com.example.sts.controller.ControllerUtils
import com.example.sts.service.AuthService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val controllerUtils: ControllerUtils,
    private val authService: AuthService
) {

    @GetMapping("/profile")
    fun getUserProfile(@RequestHeader("Authorization") authHeader: String): AuthProfileResponse {
        val user = controllerUtils.getUserInfo(authHeader)

        return AuthProfileResponse(
            uid = user.userId,
            email = user.userEmail,
            name = user.userName,
            createdAt = user.createdAt
        )
    }

    @PostMapping("/youtube-connect")
    fun getYouTubePermission(@RequestHeader("Authorization") authHeader: String,
                             @RequestBody request: AuthYouTubeConnectRequest): AuthYouTubeConnectResponse {
        val user = controllerUtils.getUserInfo(authHeader)

        val result = authService.exchangeCodeForYouTubeAccessToken(
            userId = user.userId,
            code = request.authCode
        )

        return AuthYouTubeConnectResponse(result)
    }
}