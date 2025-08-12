package com.example.sts.config.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.nio.charset.StandardCharsets

inline fun <reified T : Any> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)

class LoggingInterceptor : ClientHttpRequestInterceptor {

    private val log = logger()

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        logRequest(request, body)
        val response = execution.execute(request, body)
        // Note: Buffering the response to read the body for logging
        val bufferedResponse = BufferingClientHttpResponse(response)
        logResponse(bufferedResponse)
        return bufferedResponse
    }

    private fun logRequest(request: HttpRequest, body: ByteArray) {
        log.info("➡️ [RestTemplate Request] ${request.method} ${request.uri}")
        if (body.isNotEmpty()) {
            log.info("   Request Body: ${String(body, StandardCharsets.UTF_8)}")
        }
    }

    private fun logResponse(response: ClientHttpResponse) {
        val responseBody = response.body.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        log.info("⬅️ [RestTemplate Response] Status: ${response.statusCode}")
        if (responseBody.isNotEmpty()) {
            log.info("   Response Body: $responseBody")
        }
    }
}