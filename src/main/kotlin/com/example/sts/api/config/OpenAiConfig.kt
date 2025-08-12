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
class OpenAiConfig {

    @Value("\${openai.api.key}")
    private lateinit var apiKey: String


    @Bean
    fun openAiRestTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder
            .rootUri("https://api.openai.com/v1")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .additionalInterceptors(LoggingInterceptor())
            .build()
    }

}
