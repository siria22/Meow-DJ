package com.example.sts.exceptions

// USER

class UserNotFoundException(userId: String)
    : RuntimeException("User not found for id: $userId")


// SESSION

class SessionNotFoundException(sessionId: Long)
    : RuntimeException("Session not found for id: $sessionId")

class SessionNotBelongToUser(sessionId: Long, uid: Long)
    : RuntimeException("Session not belong to user;\n" +
        "Session id: $sessionId, userId: $uid")


// TOKEN

class TokenRefreshFailedException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class TokenNotValidException(message: String = "Access token is not valid", cause: Throwable? = null) :
    RuntimeException(message, cause)

// PLAYLIST

class PlaylistNotFoundException(playlistId: Long)
    : RuntimeException("Playlist not found for id: $playlistId")

// API
