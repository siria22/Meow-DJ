package com.example.sts.api.youtube

import com.fasterxml.jackson.annotation.JsonProperty

data class YoutubeTrackResult (
    val videoId: String,
    val publishedAt: String,
    val title: String,
)

data class YouTubeSearchResponse(
    val items: List<SearchItem>
)

data class SearchItem(
    val id: SearchItemId,
    val snippet: Snippet
)

data class SearchItemId(
    val kind: String,
    val videoId: String?
)

data class Snippet(
    val publishedAt: String,
    val channelId: String,
    val title: String,
    val description: String,
    val thumbnails: Thumbnails,
    val channelTitle: String
)

data class Thumbnails(
    val default: ThumbnailInfo?,
    val medium: ThumbnailInfo?,
    val high: ThumbnailInfo?
)

data class ThumbnailInfo(
    val url: String,
    val width: Int? = null,
    val height: Int? = null
)


// YouTube playlist
class YouTubePlaylistCreateRequest(
    val snippet: Snippet,
    val status: Status
) {
    data class Snippet(
        val title: String,
        val description: String
    )

    data class Status(
        @JsonProperty("privacyStatus")
        val privacyStatus: String // "public", "private", "unlisted"
    )
}

class YouTubePlaylistCreateResponse(
    val id: String
)

// add to playlist
data class PlaylistItemInsertRequest(
    @JsonProperty("snippet")
    val snippet: Snippet
) {
    data class Snippet(
        @JsonProperty("playlistId")
        val playlistId: String,

        @JsonProperty("resourceId")
        val resourceId: ResourceId
    )

    data class ResourceId(
        @JsonProperty("kind")
        val kind: String,

        @JsonProperty("videoId")
        val videoId: String
    )
}
