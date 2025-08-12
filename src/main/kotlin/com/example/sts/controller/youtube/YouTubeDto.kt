package com.example.sts.controller.youtube

import com.example.sts.service.model.track.DetailTrackInfo

data class AddToPlaylistRequest(
    val sessionId: Long,
    val videoIds: List<String>
)

data class AddToPlaylistResponse(
    val assistantMessageId: Long,
    val assistantMessage: String
)

// get all playlist items
data class GetAllPlaylistItemsResult(val playlistItems: List<DetailTrackInfo>)