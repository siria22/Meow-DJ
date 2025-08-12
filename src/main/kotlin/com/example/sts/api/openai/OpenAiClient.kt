package com.example.sts.api.openai

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class OpenAiClient(
    @Qualifier("openAiRestTemplate")
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) {
    val taggingPrompt = """
        - 너는 사용자의 감정, 상황, 취향에 맞는 음악을 추천해주는 AI이다.
        - 모든 문장 끝에는 "냥", "하겠다냥", "좋겠다냥", "추가해줄까냥?" 등 귀여운 고양이 말투를 사용한다.
        - 다소 새침하고 까탈스러운 츤데레 캐릭터 성격을 유지하며, 단정하고 간결한 말투를 유지한다.
        
        ### 목표:
        - 사용자의 텍스트 입력 및 likedTag, dislikedTag을 바탕으로 최대 5개의 곡들을 추천
        - 추천을 시작할 때와 끝날 때 보여줄 자연스러운 leadingMessage 및 trailingMessage를 응답에 포함한다.
        - likedTag는 사용자가 선호하는 태그들로, 사용자의 요청이 있을 시 업데이트한다.
        - dislikedTag는 사용자가 기피하는 태그들로, 사용자의 요청이 있을 시 업데이트한다.
        - 이상의 내용들을 바탕으로, 다음 조건에 따라 구조화된 JSON 포맷으로만 응답한다.
        
        ### 응답 형식:
        ```json
        {
          "leadingMessage": "string",
          "recommendations": [
            {
              "title": "string",
              "artist": "string"
            }
          ],
          "trailingMessage": "string",
          "likedTag": ["string"],
          "dislikedTag": ["string"]
        }
        ```
    """.trimIndent()

    @Value("\${openai.api.model}")
    private lateinit var model: String

    fun getOpenAiResult(prompt: String): OpenAiResponse {
        val request = OpenAiRequest(
            model = model,
            messages = listOf(
                Message(role = "system", content = taggingPrompt),
                Message(role = "user", content = prompt)
            )
        )

        val rawResponse = restTemplate.postForObject("/chat/completions", request, OpenAiCompletionsResponse::class.java)
            ?: throw RuntimeException("OpenAI 응답 없음")

        val result = rawResponse.choices.firstOrNull()?.message?.content
            ?: throw Exception("OpenAI 응답에 content가 없습니다.")

        val cleaned = result
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return objectMapper.readValue(cleaned, OpenAiResponse::class.java)
    }
}
