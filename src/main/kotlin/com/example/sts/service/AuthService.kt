package com.example.sts.service

import com.example.sts.exceptions.EmptyAccessTokenException
import com.example.sts.exceptions.TokenRefreshFailedException
import com.example.sts.exceptions.UserNotFoundException
import com.example.sts.persistence.entity.UserEntity
import com.example.sts.persistence.repository.UsersRepository
import com.example.sts.service.model.auth.TokenResponse
import com.example.sts.service.model.auth.User
import com.example.sts.service.model.toDomain
import com.google.firebase.auth.FirebaseAuth
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.time.Instant

@Service
class AuthService(
    private val usersRepository: UsersRepository,
    private val firebaseAuth: FirebaseAuth
) {
    @Value("\${google.api.key}")
    private lateinit var clientId: String

    @Value("\${google.api.secret}")
    private lateinit var clientSecret: String

    @Value("\${google.api.redirect}")
    private lateinit var redirectUri: String

    private val googleTokenUrl = "https://oauth2.googleapis.com/token"

    private val restTemplate = RestTemplate()

    fun getUserByFirebaseUidOrCreateUser(firebaseUid: String): User {
        val existingUser = usersRepository.findByFirebaseUid(firebaseUid)
        if (existingUser != null) {
            return existingUser.toDomain()
        }

        val firebaseUser = firebaseAuth.getUser(firebaseUid)
        val newUserEntity = UserEntity(
            userId = null,
            firebaseUid = firebaseUser.uid,
            userName = firebaseUser.displayName,
            userEmail = firebaseUser.email,
            youTubeAccessToken = null,
            refreshToken = null,
            expiresAt = null,
            createdAt = Instant.now()
        )

        return usersRepository.save(newUserEntity).toDomain()
    }

    fun exchangeCodeForYouTubeAccessToken(userId: Long, code: String): Boolean {
        return runCatching {
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
            }
            val requestBody: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
                add("code", code)
                add("client_id", clientId)
                add("client_secret", clientSecret)
                add("redirect_uri", redirectUri)
                add("grant_type", "authorization_code")
            }
            val requestEntity = HttpEntity(requestBody, headers)

            val response = restTemplate.postForObject(googleTokenUrl, requestEntity, TokenResponse::class.java)
                ?: throw TokenRefreshFailedException("Token exchange failed: null response")

            usersRepository.updateTokens(
                userId,
                response.accessToken,
                response.refreshToken,
                Instant.now().plusSeconds(response.expiresIn.toLong())
            )
            true
        }.getOrElse { ex ->
            throw TokenRefreshFailedException("Token exchange failed", ex)
        }
    }

    fun provideValidYouTubeAccessToken(userId: Long): String {
        val userEntity = usersRepository.findById(userId)
            .orElseThrow { UserNotFoundException(userId.toString()) }

        val user = userEntity.toDomain()

        if(user.youTubeAccessToken == null) {
            throw EmptyAccessTokenException()
        } else {
            if (user.expiresAt!!.isBefore(Instant.now())) {
                val refreshedUser = refreshGoogleAccessToken(userEntity)
                return refreshedUser.youTubeAccessToken ?: ""
            }
        }
        return user.youTubeAccessToken
    }

    private fun refreshGoogleAccessToken(user: UserEntity): UserEntity {
        return runCatching {
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
            }
            val requestBody: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
                add("client_id", clientId)
                add("client_secret", clientSecret)
                add("refresh_token", user.refreshToken)
                add("grant_type", "refresh_token")
            }
            val requestEntity = HttpEntity(requestBody, headers)

            val response = restTemplate.postForObject(googleTokenUrl, requestEntity, TokenResponse::class.java)
                ?: throw TokenRefreshFailedException("Token refresh failed: null response")

            val updatedUser = user.apply {
                this.youTubeAccessToken = response.accessToken
                this.expiresAt = Instant.now().plusSeconds(response.expiresIn.toLong())
            }
            usersRepository.save(updatedUser)
            updatedUser
        }.getOrElse { ex ->
            throw TokenRefreshFailedException("Token refresh failed", ex)
        }
    }
}