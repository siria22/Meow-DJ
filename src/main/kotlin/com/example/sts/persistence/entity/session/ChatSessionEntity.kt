package com.example.sts.persistence.entity.session

import com.example.sts.config.utils.StringListJsonConverter
import jakarta.persistence.*
import java.time.Instant
import javax.annotation.Nullable

@Entity
@Table(name = "session")
data class ChatSessionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    val sessionTitle: String,

    val lastAccessedAt: Instant,

    val ownerId: Long,

    @Nullable
    var youTubePlaylistId: String?,

    @Nullable
    var playlistId: Long?,

    @Embedded
    val sessionMemory: SessionMemoryEntity
)

@Embeddable
class SessionMemoryEntity(
    @Convert(converter = StringListJsonConverter::class)
    val likedTags: List<String>,

    @Convert(converter = StringListJsonConverter::class)
    val dislikedTags: List<String>
) {
    companion object {
        fun empty() = SessionMemoryEntity(likedTags = listOf(), dislikedTags = listOf())
    }
}
