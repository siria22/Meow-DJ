package com.example.sts.service.model

import com.example.sts.controller.chat.AssistantResponseDto
import com.example.sts.controller.chat.ChatMessagesDto
import com.example.sts.controller.chat.TextMessageDto
import com.example.sts.controller.chat.TrackDto
import com.example.sts.controller.youtube.AddToPlaylistResponse
import com.example.sts.persistence.entity.UserEntity
import com.example.sts.persistence.entity.chat.ChatMessageEntity
import com.example.sts.persistence.entity.chat.RecommendationChatMessageEntity
import com.example.sts.persistence.entity.chat.TextChatMessageEntity
import com.example.sts.persistence.entity.playlist.PlaylistEntity
import com.example.sts.persistence.entity.playlist.PlaylistItemEntity
import com.example.sts.persistence.entity.session.ChatSessionEntity
import com.example.sts.persistence.entity.session.SessionMemoryEntity
import com.example.sts.service.model.auth.User
import com.example.sts.service.model.chat.ChatSession
import com.example.sts.service.model.chat.SessionMemory
import com.example.sts.service.model.playlist.Playlist
import com.example.sts.service.model.playlist.PlaylistItem
import com.example.sts.service.model.youtube.YouTubeAddToPlaylistResult

fun ChatMessageEntity.toDto(): ChatMessagesDto {
    return when (this) {
        is TextChatMessageEntity -> TextMessageDto(
            messageId = this.id,
            senderType = this.senderType.name,
            createdAt = this.createdAt,
            content = this.content
        )

        is RecommendationChatMessageEntity -> AssistantResponseDto(
            messageId = this.id,
            senderType = this.senderType.name,
            createdAt = this.createdAt,
            leadingMessage = this.leadingMessage ?: "",
            trailingMessage = this.trailingMessage ?: "",
            tracks = this.recommendedTrackEntities.map { trackEntity ->
                TrackDto(
                    videoId = trackEntity.videoId,
                    trackTitle = trackEntity.trackTitle,
                    youtubeTitle = trackEntity.youtubeTitle,
                    artist = trackEntity.artist,
                    tags = trackEntity.tags
                )
            }
        )

        else -> throw IllegalArgumentException("Unknown message type")
    }
}

fun YouTubeAddToPlaylistResult.toDto(): AddToPlaylistResponse {
    return AddToPlaylistResponse(
        assistantMessageId = this.assistantMessageId,
        assistantMessage = this.assistantMessage
    )
}

fun UserEntity.toDomain(): User = User(
    userId = this.userId,
    userName = this.userName,
    userEmail = this.userEmail,
    createdAt = this.createdAt
)

fun ChatSessionEntity.toDomain(): ChatSession = ChatSession(
    id = this.id!!,
    sessionTitle = this.sessionTitle,
    lastAccessedAt = this.lastAccessedAt,
    ownerId = this.ownerId,
    youTubePlaylistId = this.youTubePlaylistId,
    playlistId = this.playlistId,
    sessionMemory = SessionMemory(
        likedTags = this.sessionMemory.likedTags,
        dislikedTags = this.sessionMemory.dislikedTags
    )
)

fun ChatSession.toEntity(): ChatSessionEntity = ChatSessionEntity(
    id = this.id,
    sessionTitle = this.sessionTitle,
    lastAccessedAt = this.lastAccessedAt,
    ownerId = this.ownerId,
    youTubePlaylistId = this.youTubePlaylistId,
    playlistId = this.playlistId,
    sessionMemory = SessionMemoryEntity(
        likedTags = this.sessionMemory.likedTags,
        dislikedTags = this.sessionMemory.dislikedTags
    )
)

fun PlaylistEntity.toDomain(): Playlist = Playlist(
    id = this.id!!,
    youTubePlaylistId = this.youTubePlaylistId,
    playlistItems = this.playlistItems.map { it.toDomain() }
)

fun PlaylistItemEntity.toDomain(): PlaylistItem = PlaylistItem(
    id = this.id,
    videoId = this.videoId,
    fav = this.fav
)

fun Playlist.toEntity(): PlaylistEntity {
    val playlistEntity = PlaylistEntity(
        id = this.id,
        youTubePlaylistId = this.youTubePlaylistId
    )

    val items = this.playlistItems.map {
        it.toEntity(playlistEntity)
    }

    playlistEntity.playlistItems.addAll(items)
    return playlistEntity
}

fun PlaylistItem.toEntity(playlist: PlaylistEntity): PlaylistItemEntity = PlaylistItemEntity(
    id = this.id,
    videoId = this.videoId,
    fav = this.fav,
    playlist = playlist
)