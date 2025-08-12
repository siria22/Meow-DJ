package com.example.sts.persistence.repository

import com.example.sts.persistence.entity.session.ChatSessionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SessionRepository : JpaRepository<ChatSessionEntity, Long?> {

    fun save(chatSessionEntity: ChatSessionEntity): ChatSessionEntity

    fun findByOwnerId(ownerId: Long): List<ChatSessionEntity>?
}