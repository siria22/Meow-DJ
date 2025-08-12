package com.example.sts.service

import com.example.sts.api.lastfm.LastFmClient
import com.example.sts.api.openai.OpenAiBasicTrackInfo
import com.example.sts.api.openai.OpenAiClient
import com.example.sts.api.openai.OpenAiResponse
import com.example.sts.api.youtube.YouTubeClient
import com.example.sts.api.youtube.YoutubeTrackResult
import com.example.sts.persistence.entity.chat.ChatMessageEntity
import com.example.sts.persistence.entity.chat.RecommendationChatMessageEntity
import com.example.sts.persistence.entity.chat.TextChatMessageEntity
import com.example.sts.persistence.entity.playlist.PlaylistEntity
import com.example.sts.persistence.entity.session.ChatSessionEntity
import com.example.sts.persistence.entity.session.SessionMemoryEntity
import com.example.sts.persistence.repository.ChatMessageRepository
import com.example.sts.persistence.repository.PlaylistItemRepository
import com.example.sts.persistence.repository.PlaylistRepository
import com.example.sts.persistence.repository.SessionRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

// ChatServiceTest.kt
@ExtendWith(MockKExtension::class)
internal class ChatServiceTest {

    // mocks
    @MockK
    private lateinit var authService: AuthService

    @MockK
    private lateinit var sessionRepository: SessionRepository

    @MockK
    private lateinit var playlistRepository: PlaylistRepository

    @MockK
    private lateinit var playlistItemRepository: PlaylistItemRepository

    @MockK
    private lateinit var chatMessageRepository: ChatMessageRepository

    @MockK
    private lateinit var openAiClient: OpenAiClient

    @MockK
    private lateinit var youTubeClient: YouTubeClient

    @MockK
    private lateinit var lastFmClient: LastFmClient

    // Spy 또는 실제 테스트 대상
    @InjectMockKs
    lateinit var chatService: ChatService  // → sendChatMessage() 가 정의된 서비스

    @Test
    fun `처음 세션을 생성하는 케이스`() {

        //given
        val uid = 100L
        val sessionId = -1L
        val prompt = "기분 좋은 저녁에 어울리는 음악 추천해줘"

        val expectedSessionId = 1L
        val expectedUserMessageId = 101L
        val expectedAssistantMessageId = 102L
        val expectedYouTubePlaylistId = "new-youtube-playlist-id"
        val expectedLeadingMessage = "this is leading message"
        val expectedTrailingMessage = "this is trailing message"

        // access token
        every { authService.provideValidYouTubeAccessToken(uid) } returns "fake-access-token"

        // create new session
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val newSessionEntity = spyk(
            ChatSessionEntity(
                id = null,
                ownerId = uid,
                sessionTitle = "${today}의 채팅",
                lastAccessedAt = Instant.now(),
                youTubePlaylistId = null,
                playlistId = null,
                sessionMemory = SessionMemoryEntity.empty(),
            )
        )
        every { sessionRepository.findById(-1L) } returns Optional.empty()
        every { sessionRepository.save(any()) } answers {
            val entityToSave = firstArg<ChatSessionEntity>()
            spyk(entityToSave).apply {
                every { id } returns expectedSessionId
            }
        }
        every { playlistRepository.findById(-1L) } returns Optional.of(
            PlaylistEntity(
                id = 1L,
                youTubePlaylistId = expectedYouTubePlaylistId,
                playlistItems = mutableListOf()
            )
        )

        // new playlist
        val sessionAfterSave = spyk(newSessionEntity)
        every { sessionRepository.findById(expectedSessionId) } returns Optional.of(sessionAfterSave)
        every { youTubeClient.createPlaylist(any(), any(), any()) } returns expectedYouTubePlaylistId

        // save message
        every { chatMessageRepository.save(any()) } answers {
            val messageEntity = firstArg<ChatMessageEntity>()
            when (messageEntity) {
                is TextChatMessageEntity -> {
                    spyk(messageEntity).apply {
                        every { id } returns expectedUserMessageId
                    }
                }

                is RecommendationChatMessageEntity -> {
                    spyk(messageEntity).apply {
                        every { id } returns expectedAssistantMessageId
                    }
                }

                else -> firstArg() // 예상치 못한 타입은 그대로 반환
            }
        }

        // external apis
        every { openAiClient.getOpenAiResult(any()) } returns OpenAiResponse(
            leadingMessage = expectedLeadingMessage,
            trailingMessage = expectedTrailingMessage,
            recommendations = listOf(OpenAiBasicTrackInfo(title = "Track 1", artist = "Artist 1")),
            likedTag = listOf("chill"), dislikedTag = emptyList()
        )
        every { youTubeClient.getVideoId(any()) } returns YoutubeTrackResult("video-id", "2025-01-01", "YT Title")
        every { lastFmClient.getTrackTags(any(), any()) } returns mutableListOf("chill", "relaxing")


        // when
        val result = chatService.sendChatMessage(uid, sessionId, prompt)

        // then
        assertEquals(expectedSessionId, result.sessionId)
        assertEquals(expectedUserMessageId, result.userMessageId)
        assertEquals(expectedAssistantMessageId, result.recommendationMessageId)
        assertEquals(expectedLeadingMessage, result.recommendation.leadingMessage)
        assertEquals(expectedTrailingMessage, result.recommendation.trailingMessage)
        assertTrue(result.isSessionUpdateNeeded)
    }
}