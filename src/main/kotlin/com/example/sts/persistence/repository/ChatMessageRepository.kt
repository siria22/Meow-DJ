package com.example.sts.persistence.repository

import com.example.sts.persistence.entity.chat.ChatMessageEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository : JpaRepository<ChatMessageEntity, Long> {
    // sessionId만 알고 있을 때
    fun findTop30BySessionIdOrderByIdDesc(sessionId: Long): List<ChatMessageEntity>

    // 마지막 ID 알고 있을 때
    fun findTop30BySessionIdAndIdLessThanOrderByIdDesc(sessionId: Long, id: Long): List<ChatMessageEntity>

    fun save(chatMessageEntity: ChatMessageEntity): ChatMessageEntity
}