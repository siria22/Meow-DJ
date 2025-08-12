package com.example.sts.service.model.track

data class DetailTrackInfo(
    val id: Long,
    val videoId: String,
    val trackTitle: String,
    val youtubeTitle: String,
    val artist: String,
    val tags: MutableList<String>
)