package com.example.sts.service.model.chat

import com.example.sts.service.model.track.DetailTrackInfo
import java.time.Instant

interface ChatMessage {
    val messageId: Long
    val sessionId: Long
    val senderType: String
    val createdAt: Instant
}

data class TextMessage(
    override val messageId: Long,
    override val sessionId: Long,
    override val senderType: String,
    override val createdAt: Instant,
    val content: String
) : ChatMessage

data class RecommendationMessage(
    override val messageId: Long,
    override val sessionId: Long,
    override val senderType: String,
    override val createdAt: Instant,
    val content: Recommendation
) : ChatMessage

data class Recommendation(
    val leadingMessage: String,
    val trailingMessage: String,
    val detailTrackInfos: MutableList<DetailTrackInfo>
)