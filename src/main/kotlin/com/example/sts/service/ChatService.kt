package com.example.sts.service

import com.example.sts.api.lastfm.LastFmClient
import com.example.sts.api.openai.OpenAiBasicTrackInfo
import com.example.sts.api.openai.OpenAiClient
import com.example.sts.api.youtube.YouTubeClient
import com.example.sts.controller.chat.ChatMessagesDto
import com.example.sts.exceptions.SessionNotBelongToUser
import com.example.sts.exceptions.SessionNotFoundException
import com.example.sts.persistence.entity.chat.*
import com.example.sts.persistence.entity.playlist.PlaylistEntity
import com.example.sts.persistence.entity.session.ChatSessionEntity
import com.example.sts.persistence.entity.session.SessionMemoryEntity
import com.example.sts.persistence.repository.ChatMessageRepository
import com.example.sts.persistence.repository.PlaylistItemRepository
import com.example.sts.persistence.repository.PlaylistRepository
import com.example.sts.persistence.repository.SessionRepository
import com.example.sts.service.model.chat.ChatSendResult
import com.example.sts.service.model.chat.ChatSession
import com.example.sts.service.model.chat.Recommendation
import com.example.sts.service.model.playlist.Playlist
import com.example.sts.service.model.playlist.PlaylistItem
import com.example.sts.service.model.toDomain
import com.example.sts.service.model.toDto
import com.example.sts.service.model.toEntity
import com.example.sts.service.model.track.DetailTrackInfo
import com.example.sts.service.model.youtube.YouTubeAddToPlaylistResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.jvm.optionals.getOrNull

@Service
class ChatService(
    private val authService: AuthService,
    private val sessionRepository: SessionRepository,
    private val playlistRepository: PlaylistRepository,
    private val playlistItemRepository: PlaylistItemRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val openAiClient: OpenAiClient,
    private val youTubeClient: YouTubeClient,
    private val lastFmClient: LastFmClient,
) {
    private fun getOrCreateNewSession(uid: Long, sessionId: Long): ChatSession {

        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val chatSessionEntity = sessionRepository.findById(sessionId).getOrNull()
            ?: sessionRepository.save(
                ChatSessionEntity(
                    id = null,
                    sessionTitle = "${today}의 채팅",
                    lastAccessedAt = Instant.now(),
                    ownerId = uid,
                    youTubePlaylistId = null,
                    playlistId = null,
                    sessionMemory = SessionMemoryEntity.empty(),
                )
            )
        return chatSessionEntity.toDomain()
    }

    private fun getOrCreateNewYouTubePlayList(accessToken: String, session: ChatSession): Pair<ChatSession, Boolean> {

        if (session.youTubePlaylistId != null) {
            return Pair(session, false)
        }

        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val newYouTubePlayListId = youTubeClient.createPlaylist(
            accessToken = accessToken,
            title = "Meow - playlist",
            description = "created by NyanDJ at $today"
        )

        val updatedSessionEntity = sessionRepository.save(
            session.toEntity().copy(
                youTubePlaylistId = newYouTubePlayListId
            )
        )

        return Pair(updatedSessionEntity.toDomain(), true)
    }

    private fun getOrCreateNewPlaylist(session: ChatSession): Pair<Playlist, Boolean> {
        var isPlaylistCreated = false

        val playlistId = session.playlistId ?: -1L
        val playlist = playlistRepository.findById(playlistId).getOrNull()
            ?: run {
                val newPlaylistEntity = PlaylistEntity(
                    id = null,
                    youTubePlaylistId = session.youTubePlaylistId!!,
                    playlistItems = mutableListOf(),
                )

                val savedPlaylistEntity = playlistRepository.save(newPlaylistEntity)

                sessionRepository.save(
                    chatSessionEntity = session.toEntity().copy(
                        playlistId = savedPlaylistEntity.id
                    )
                )
                isPlaylistCreated = true
                savedPlaylistEntity
            }

        return Pair(playlist.toDomain(), isPlaylistCreated)
    }

    private fun createDetailTrackInfos(tracks: List<OpenAiBasicTrackInfo>): MutableList<DetailTrackInfo> {
        return tracks.map { track ->
            val queryMessage = track.title + " " + track.artist
            val video = youTubeClient.getVideoId(queryMessage)

            val tags: MutableList<String> = lastFmClient.getTrackTags(track.artist, track.title)

            DetailTrackInfo(
                id = 0L,
                videoId = video.videoId,
                trackTitle = track.title,
                youtubeTitle = video.title,
                artist = track.artist,
                tags = tags
            )
        } as MutableList<DetailTrackInfo>
    }

    fun toSenderType(senderType: String): SenderType = when (senderType) {
        "user" -> SenderType.USER
        "assistant" -> SenderType.ASSISTANT
        else -> throw IllegalArgumentException("Unknown sender type: $senderType")
    }

    private fun saveChatMessage(
        sessionId: Long,
        senderType: String,
        messageType: ChatMessageType,
        content: Any
    ): Long {

        val currentSession = sessionRepository.findById(sessionId).getOrNull()
            ?: throw SessionNotFoundException(sessionId = sessionId)

        if (messageType == ChatMessageType.TEXT && content is String) {
            val savedMessage = chatMessageRepository.save(
                TextChatMessageEntity(
                    content = content,
                    session = currentSession,
                    sender = toSenderType(senderType),
                    createdAt = Instant.now()
                )
            )
            return savedMessage.id
        } else if (messageType == ChatMessageType.RECOMMENDATION && content is Recommendation) {
            val recommendationMessage = RecommendationChatMessageEntity(
                session = currentSession,
                senderType = toSenderType(senderType),
                createdAt = Instant.now(),
                leadingMessage = content.leadingMessage,
                trailingMessage = content.trailingMessage
            )

            val trackEntities = content.detailTrackInfos.map { trackInfo ->
                RecommendedTrackEntity(
                    assistantResponse = recommendationMessage,
                    videoId = trackInfo.videoId,
                    trackTitle = trackInfo.trackTitle,
                    youtubeTitle = trackInfo.youtubeTitle,
                    artist = trackInfo.artist,
                    tags = trackInfo.tags
                )
            }

            recommendationMessage.recommendedTrackEntities = trackEntities as MutableList<RecommendedTrackEntity>

            val savedMessage = chatMessageRepository.save(recommendationMessage)
            return savedMessage.id
        } else throw IllegalArgumentException("Unsupported message type: $content")
    }

    fun sendChatMessage(uid: Long, sessionId: Long, prompt: String): ChatSendResult {

        return try {
            val accessToken = authService.provideValidYouTubeAccessToken(uid)
            val initialChatSession = getOrCreateNewSession(uid, sessionId)
            val (chatSession, isSessionUpdateNeeded) = getOrCreateNewYouTubePlayList(accessToken, initialChatSession)

            val userMessageEntityId = saveChatMessage(
                sessionId = chatSession.id!!,
                senderType = "user",
                messageType = ChatMessageType.TEXT,
                content = prompt
            )

            val sessionMemory = chatSession.sessionMemory
            val message = prompt + "\n" + sessionMemory.toMessageHistory()
            val openAiResponse = openAiClient.getOpenAiResult(message)

            val leadingMessage = openAiResponse.leadingMessage
            val trailingMessage = openAiResponse.trailingMessage
            val detailTrackInfos = createDetailTrackInfos(openAiResponse.recommendations)

            sessionRepository.save(
                chatSession.toEntity().copy(
                    youTubePlaylistId = chatSession.youTubePlaylistId,
                    playlistId = chatSession.playlistId,
                    sessionMemory = SessionMemoryEntity(
                        likedTags = openAiResponse.likedTag,
                        dislikedTags = openAiResponse.dislikedTag
                    )
                )
            )

            val recommendation = Recommendation(
                leadingMessage = leadingMessage,
                trailingMessage = trailingMessage,
                detailTrackInfos = detailTrackInfos
            )

            val recommendationMessageEntityId = saveChatMessage(
                sessionId = chatSession.id,
                senderType = "assistant",
                messageType = ChatMessageType.RECOMMENDATION,
                content = Recommendation(
                    leadingMessage = leadingMessage,
                    trailingMessage = trailingMessage,
                    detailTrackInfos = detailTrackInfos
                )
            )

            ChatSendResult(
                sessionId = chatSession.id,
                userMessageId = userMessageEntityId,
                recommendationMessageId = recommendationMessageEntityId,
                recommendation = recommendation,
                isSessionUpdateNeeded = isSessionUpdateNeeded
            )

        } catch (ex: Exception) {
            throw ex
        }
    }

    @Transactional
    fun addToPlaylist(uid: Long, sessionId: Long, videoIds: List<String>): YouTubeAddToPlaylistResult {

        val accessToken = authService.provideValidYouTubeAccessToken(uid)
        val initialChatSession = getOrCreateNewSession(uid, sessionId)
        val (chatSession, _) = getOrCreateNewYouTubePlayList(accessToken, initialChatSession)
        val (playlist, _) = getOrCreateNewPlaylist(initialChatSession)

        videoIds.forEach { videoId ->
            youTubeClient.addVideoToPlaylist(
                accessToken = accessToken,
                playlistId = chatSession.youTubePlaylistId ?: "",
                videoId = videoId
            )

            val playlistItem = PlaylistItem(
                id = null,
                videoId = videoId,
                fav = false
            )

            playlistItemRepository.save(playlistItem.toEntity(playlist.toEntity()))
        }

        val assistantMessage = "선택하신 곡들이 추가되었다냥."

        val assistantMessageEntityId = saveChatMessage(
            sessionId = chatSession.id!!,
            senderType = "assistant",
            messageType = ChatMessageType.TEXT,
            content = assistantMessage
        )

        return YouTubeAddToPlaylistResult(
            assistantMessageId = assistantMessageEntityId,
            assistantMessage = assistantMessage,
        )
    }

    fun getSessions(uid: Long): List<ChatSession> {
        val sessions = runCatching { sessionRepository.findByOwnerId(uid) }.getOrNull()

        return sessions?.map { it.toDomain() } ?: emptyList()
    }

    fun getChatMessages(sessionId: Long, lastReceivedMessageId: Long): List<ChatMessagesDto> {

        val messages: List<ChatMessageEntity> =
            if (lastReceivedMessageId == 0L) {
                chatMessageRepository.findTop30BySessionIdOrderByIdDesc(sessionId)
            } else {
                chatMessageRepository.findTop30BySessionIdAndIdLessThanOrderByIdDesc(sessionId, lastReceivedMessageId)
            }

        return messages.map { it.toDto() }.reversed()
    }

    fun getSessionInfo(uid: Long, sessionId: Long): ChatSession {
        val session =
            sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException(sessionId = sessionId) }

        if (session.ownerId != uid) {
            throw SessionNotBelongToUser(sessionId = sessionId, uid = uid)
        }

        return session.toDomain()
    }

}

enum class ChatMessageType {
    TEXT, RECOMMENDATION
}