package com.example.sts.persistence.repository

import com.example.sts.persistence.entity.UserEntity
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface UsersRepository : JpaRepository<UserEntity, Long> {
    fun findByFirebaseUid(firebaseUid: String): UserEntity?

    @Transactional
    @Modifying
    @Query(
        """
        UPDATE UserEntity u 
        SET u.youTubeAccessToken = :accessToken, 
            u.refreshToken = :refreshToken, 
            u.expiresAt = :expiresAt 
        WHERE u.userId = :userId
    """
    )
    fun updateTokens(
        @Param("userId") userId: Long,
        @Param("accessToken") accessToken: String?,
        @Param("refreshToken") refreshToken: String?,
        @Param("expiresAt") expiresAt: Instant
    ): Int
}
