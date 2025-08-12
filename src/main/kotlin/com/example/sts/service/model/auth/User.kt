package com.example.sts.service.model.auth

data class User(
    val userId: Long,
    val userName: String,
    val userEmail: String,
    val createdAt: String
)
