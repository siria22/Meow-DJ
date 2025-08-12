package com.example.sts.persistence.entity.chat

import com.example.sts.persistence.entity.session.ChatSessionEntity
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "message") // <- "TEXT_MESSAGE", "RECOMMENDATION_MESSAGE"
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "message_type")
abstract class ChatMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    val session: ChatSessionEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    val senderType: SenderType,  // "USER", "ASSISTANT"

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)

enum class SenderType {
    USER,
    ASSISTANT
}
