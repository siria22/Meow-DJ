package com.example.sts.api.config

import com.example.sts.config.utils.LoggingInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Configuration
class LastFmConfig {

    @Value("\${lastfm.api.key}")
    private lateinit var apiKey: String

    @Bean
    fun lastFmRestTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder
            .rootUri("http://ws.audioscrobbler.com/2.0")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .additionalInterceptors(apiKeyInterceptor(), LoggingInterceptor())
            .build()
    }

    private fun apiKeyInterceptor(): ClientHttpRequestInterceptor {
        return ClientHttpRequestInterceptor { request, body, execution ->
            val uri = UriComponentsBuilder
                .fromUri(request.uri)
                .queryParam("api_key", apiKey)
                .queryParam("format", "json")
                .build(true)
                .toUri()

            val newRequest = object : org.springframework.http.client.support.HttpRequestWrapper(request) {
                override fun getURI(): URI {
                    return uri
                }
            }
            execution.execute(newRequest, body)
        }
    }
}
