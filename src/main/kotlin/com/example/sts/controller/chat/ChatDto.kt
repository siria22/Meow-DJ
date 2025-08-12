package com.example.sts.controller.chat

import com.example.sts.service.model.chat.ChatSession
import com.example.sts.service.model.chat.Recommendation

data class ChatSendMessageRequest(
    val sessionId: Long,
    val prompt: String
)

data class ChatSendMessageResponse(
    val sessionId: Long,
    val userMessageId: Long,
    val recommendationMessageId: Long,
    val assistantMessage: Recommendation,
    val isSessionUpdateNeeded: Boolean
)

data class ChatSessionsResponse(
    val chatSessions: List<ChatSession>
)

data class ChatSessionResponse(
    val chatSession: ChatSession
)

data class ChatMessagesRequest(
    val sessionId: Long,
    val lastReceivedMessageId: Long
)

data class ChatMessagesResponse(
    val messages: List<ChatMessagesDto>
)

sealed interface ChatMessagesDto {
    val type: String
}

data class TextMessageDto(
    override val type: String = "TEXT",
    val messageId: Long,
    val senderType: String, // "USER" or "ASSISTANT"
    val createdAt: java.time.Instant,
    val content: String
) : ChatMessagesDto

data class AssistantResponseDto(
    override val type: String = "RECOMMENDATION",
    val messageId: Long,
    val senderType: String, // 항상 "ASSISTANT"
    val createdAt: java.time.Instant,
    val leadingMessage: String,
    val trailingMessage: String,
    val tracks: List<TrackDto>
) : ChatMessagesDto

data class TrackDto(
    val videoId: String,
    val trackTitle: String,
    val youtubeTitle: String,
    val artist: String,
    val tags: List<String>
)
