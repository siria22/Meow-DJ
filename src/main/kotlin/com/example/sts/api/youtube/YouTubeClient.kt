package com.example.sts.api.youtube

import com.example.sts.api.config.YouTubeConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class YouTubeClient(
    @Qualifier("youtubeRestTemplate")
    private val restTemplate: RestTemplate,
    private val youTubeConfig: YouTubeConfig
) {

    fun getVideoId(queryMessage: String): YoutubeTrackResult {
        val apiKey = youTubeConfig.getApiKey()

        val uri = UriComponentsBuilder
            .fromPath("/search")
            .queryParam("part", "snippet")
            .queryParam("q", queryMessage)
            .queryParam("type", "video")
            .queryParam("maxResults", 1)
            .queryParam("key", apiKey)
            .build()
            .toUriString()

        val response = restTemplate.getForObject(uri, YouTubeSearchResponse::class.java)
            ?: throw IllegalStateException("YouTube search failed")

        return YoutubeTrackResult(
            videoId = response.items.firstOrNull()?.id?.videoId ?: "",
            title = response.items.firstOrNull()?.snippet?.title ?: "",
            publishedAt = response.items.firstOrNull()?.snippet?.publishedAt ?: ""
        )
    }

    fun createPlaylist(accessToken: String, title: String, description: String): String {
        val uri = UriComponentsBuilder
            .fromPath("/playlists")
            .queryParam("part", "snippet,status")
            .build()
            .toUriString()

        val headers = HttpHeaders().apply {
            setBearerAuth(accessToken)
            contentType = MediaType.APPLICATION_JSON
        }

        val requestBody = YouTubePlaylistCreateRequest(
            snippet = YouTubePlaylistCreateRequest.Snippet(
                title = title,
                description = description
            ),
            status = YouTubePlaylistCreateRequest.Status("private")
        )

        val requestEntity = HttpEntity(requestBody, headers)

        val response = restTemplate.postForObject(uri, requestEntity, YouTubePlaylistCreateResponse::class.java)

        return response?.id ?: throw IllegalStateException("Failed to create playlist")
    }

    fun addVideoToPlaylist(accessToken: String, playlistId: String, videoId: String) {
        if (playlistId.isBlank() || videoId.isBlank()) {
            throw IllegalStateException("Invalid playlistId or videoId")
        }

        val uri = UriComponentsBuilder
            .fromPath("/playlistItems")
            .queryParam("part", "snippet")
            .build()
            .toUriString()

        val headers = HttpHeaders().apply {
            setBearerAuth(accessToken)
            contentType = MediaType.APPLICATION_JSON
        }

        val requestBody = PlaylistItemInsertRequest(
            snippet = PlaylistItemInsertRequest.Snippet(
                playlistId = playlistId,
                resourceId = PlaylistItemInsertRequest.ResourceId(
                    kind = "youtube#video",
                    videoId = videoId
                )
            )
        )

        val requestEntity = HttpEntity(requestBody, headers)

        try {
            restTemplate.postForObject(uri, requestEntity, Void::class.java)
        } catch (e: HttpClientErrorException) {
            throw IllegalStateException("YouTube API error: ${e.responseBodyAsString}", e)
        }
    }
}