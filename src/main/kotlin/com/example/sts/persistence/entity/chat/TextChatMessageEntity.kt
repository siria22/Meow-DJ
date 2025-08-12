package com.example.sts.persistence.entity.chat

import com.example.sts.persistence.entity.session.ChatSessionEntity
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "text_messages")
@DiscriminatorValue("TEXT")
class TextChatMessageEntity(
    @Column(columnDefinition = "TEXT", nullable = false)
    val content: String,

    session: ChatSessionEntity,
    sender: SenderType,
    createdAt: Instant = Instant.now()
) : ChatMessageEntity(
    session = session,
    senderType = sender,
    createdAt = createdAt
)
