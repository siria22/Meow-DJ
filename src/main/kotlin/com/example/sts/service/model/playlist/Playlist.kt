package com.example.sts.service.model.playlist

data class Playlist(
    val id: Long,
    val youTubePlaylistId: String,
    val playlistItems: List<PlaylistItem>
)

data class PlaylistItem(
    val id: Long?,
    val videoId: String,
    val fav: Boolean,
)