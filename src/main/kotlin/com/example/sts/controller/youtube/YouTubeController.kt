package com.example.sts.controller.youtube

import com.example.sts.controller.ControllerUtils
import com.example.sts.service.ChatService
import com.example.sts.service.model.toDto
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/youtube")
class YouTubeController(
    private val controllerUtils: ControllerUtils,
    private val chatService: ChatService
) {

    @PostMapping("/playlist/tracks")
    fun addTracksToPlaylist(
        @RequestBody request: AddToPlaylistRequest,
        @RequestHeader("Authorization") authHeader: String
    ): AddToPlaylistResponse {
        val user = controllerUtils.getUserInfo(authHeader)
        val result = chatService.addToPlaylist(user.userId, request.sessionId, request.videoIds)
        return result.toDto()

    }

}
