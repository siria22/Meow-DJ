package com.example.sts.api.openai

import com.fasterxml.jackson.annotation.JsonProperty

data class OpenAiResponse(
    val leadingMessage: String,
    val recommendations: List<OpenAiBasicTrackInfo>,
    val trailingMessage: String,
    val likedTag: List<String>,
    val dislikedTag: List<String>
)

data class OpenAiBasicTrackInfo(
    val title: String,
    val artist: String,
)

data class OpenAiRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double = 0.7
)

data class Message(
    val role: String,
    val content: String
)

data class OpenAiCompletionsResponse(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("object")
    val obj: String,
    @JsonProperty("created")
    val created: Long,
    @JsonProperty("model")
    val model: String,
    @JsonProperty("choices")
    val choices: List<Choice>,
    @JsonProperty("usage")
    val usage: Usage
) {
    data class Choice(
        @JsonProperty("index")
        val index: Int,
        @JsonProperty("message")
        val message: Message,
        @JsonProperty("finish_reason")
        val finishReason: String
    )

    data class Message(
        @JsonProperty("role")
        val role: String,
        @JsonProperty("content")
        val content: String
    )

    data class Usage(
        @JsonProperty("prompt_tokens")
        val promptTokens: Int,
        @JsonProperty("completion_tokens")
        val completionTokens: Int,
        @JsonProperty("total_tokens")
        val totalTokens: Int
    )
}