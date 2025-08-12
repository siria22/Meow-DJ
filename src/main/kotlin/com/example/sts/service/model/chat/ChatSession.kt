package com.example.sts.service.model.chat

import java.time.Instant

data class ChatSession(
    val id: Long?,
    val sessionTitle: String,
    val lastAccessedAt: Instant,
    val ownerId: Long,
    val youTubePlaylistId: String?,
    val playlistId: Long?,
    val sessionMemory: SessionMemory,
)

data class SessionMemory(
    val likedTags: List<String>,
    val dislikedTags: List<String>
) {
    fun toMessageHistory(): String =
        "liked tags: ${likedTags.joinToString(", ")}\n" +
                "disliked tags: ${dislikedTags.joinToString(", ")}"

    companion object {
        fun empty(): SessionMemory = SessionMemory(
            likedTags = emptyList(),
            dislikedTags = emptyList()
        )
    }
}
