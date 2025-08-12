package com.example.sts.persistence.repository

import com.example.sts.persistence.entity.playlist.PlaylistItemEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PlaylistItemRepository : JpaRepository<PlaylistItemEntity, Long>{

    fun save(playlistItemEntity: PlaylistItemEntity): PlaylistItemEntity

    fun getAllByPlaylistId(playlistId: Long): List<PlaylistItemEntity>
}