package com.example.sts.persistence.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "users")
class UserEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userId: Long?,

    @Column(unique = true, nullable = false)
    val firebaseUid: String,

    var userName: String,

    val userEmail: String,

    var youTubeAccessToken: String?,

    var refreshToken: String?,

    var expiresAt: Instant?,

    var createdAt: Instant
)