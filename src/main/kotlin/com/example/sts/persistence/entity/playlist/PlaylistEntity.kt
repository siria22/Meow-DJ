package com.example.sts.persistence.entity.playlist

import jakarta.persistence.*

@Entity
@Table(name = "playlist")
data class PlaylistEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val youTubePlaylistId: String,

    @OneToMany(mappedBy = "playlist", cascade = [CascadeType.ALL], orphanRemoval = true)
    val playlistItems: MutableList<PlaylistItemEntity> = mutableListOf()
)