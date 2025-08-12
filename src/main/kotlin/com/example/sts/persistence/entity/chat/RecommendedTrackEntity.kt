package com.example.sts.persistence.entity.chat

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "recommended_tracks")
class RecommendedTrackEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_message_id", nullable = false)
    val assistantResponse: RecommendationChatMessageEntity,

    val videoId: String,
    val trackTitle: String,
    val youtubeTitle: String,
    val artist: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recommended_track_tags", joinColumns = [JoinColumn(name = "track_id")])
    @Column(name = "tag")
    val tags: MutableList<String>
)