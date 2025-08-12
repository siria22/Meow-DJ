package com.example.sts.service.model.chat

data class ChatSendResult(
    val sessionId: Long,
    val userMessageId: Long,
    val recommendationMessageId: Long,
    val recommendation: Recommendation,
    val isSessionUpdateNeeded: Boolean
)