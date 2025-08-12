package com.example.sts.controller.chat

import com.example.sts.controller.ControllerUtils
import com.example.sts.service.ChatService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val controllerUtils: ControllerUtils,
    private val chatService: ChatService
) {

    @PostMapping("/messages")
    fun sendMessage(
        @RequestBody request: ChatSendMessageRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ChatSendMessageResponse {
        val user = controllerUtils.getUserInfo(authHeader)

        val result = chatService
            .sendChatMessage(user.userId, request.sessionId, request.prompt)

        return ChatSendMessageResponse(
            sessionId = result.sessionId,
            userMessageId = result.userMessageId,
            recommendationMessageId = result.recommendationMessageId,
            assistantMessage = result.recommendation,
            isSessionUpdateNeeded = result.isSessionUpdateNeeded
        )
    }

    @GetMapping("/sessions")
    fun getChatSessions(@RequestHeader("Authorization") authHeader: String): ChatSessionsResponse {
        val user = controllerUtils.getUserInfo(authHeader)

        val result = chatService.getSessions(user.userId)
        return ChatSessionsResponse(result)
    }

    @GetMapping("/sessions/{sessionId}")
    fun getChatSessionInfo(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable sessionId: Long
    ): ChatSessionResponse {
        val user = controllerUtils.getUserInfo(authHeader)

        val result = chatService.getSessionInfo(user.userId, sessionId)
        return ChatSessionResponse(result)
    }

    @GetMapping("/sessions/{sessionId}/messages")
    fun getChatMessages(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable sessionId: Long,
        @RequestParam(required = false, defaultValue = "0") lastMessageId: Long
    ): ChatMessagesResponse {
        val messages = chatService.getChatMessages(sessionId, lastMessageId)

        return ChatMessagesResponse(messages)
    }
}
