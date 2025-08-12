package com.example.sts.persistence.repository

import com.example.sts.persistence.entity.playlist.PlaylistEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PlaylistRepository : JpaRepository<PlaylistEntity, Long> {

    fun save(playlistEntity: PlaylistEntity): PlaylistEntity
}