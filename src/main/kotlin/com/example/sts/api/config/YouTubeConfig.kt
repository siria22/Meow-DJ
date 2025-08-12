package com.example.sts.api.config

import com.example.sts.config.utils.LoggingInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate

@Configuration
class YouTubeConfig {

    @Value("\${youtube.api.key}")
    private lateinit var apiKey: String

    fun getApiKey(): String = apiKey

    @Bean
    fun youtubeRestTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder
            .rootUri("https://www.googleapis.com/youtube/v3")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .additionalInterceptors(LoggingInterceptor())
            .build()
    }
}