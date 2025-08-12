package com.example.sts.persistence.entity.chat

import com.example.sts.persistence.entity.session.ChatSessionEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "recommendation_message")
@DiscriminatorValue("RECOMMENDATION")
class RecommendationChatMessageEntity(
    session: ChatSessionEntity,
    senderType: SenderType,
    createdAt: Instant = Instant.now(),
    leadingMessage: String?,
    trailingMessage: String?
) : ChatMessageEntity(
    session = session,
    senderType = senderType,
    createdAt = createdAt
) {
    @Column(columnDefinition = "TEXT")
    var leadingMessage: String? = leadingMessage

    @Column(columnDefinition = "TEXT")
    var trailingMessage: String? = trailingMessage

    @OneToMany(mappedBy = "assistantResponse", cascade = [CascadeType.ALL], orphanRemoval = true)
    var recommendedTrackEntities: MutableList<RecommendedTrackEntity> = mutableListOf()
}