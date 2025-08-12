package com.example.sts.config.utils

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpResponse
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * ClientHttpResponse의 응답 본문을 버퍼링하는 래퍼 클래스입니다.
 * RestTemplate 로깅 인터셉터 등에서 응답 스트림을 여러 번 읽어야 할 때 사용됩니다.
 * InputStream은 한 번만 읽을 수 있으므로, 내용을 byte 배열에 복사해두고 재사용합니다.
 */
class BufferingClientHttpResponse(private val response: ClientHttpResponse) : ClientHttpResponse {

    @Volatile private var body: ByteArray? = null

    // getStatusCode의 반환 타입을 HttpStatusCode로 변경
    override fun getStatusCode(): HttpStatusCode = response.statusCode

    override fun getStatusText(): String = response.statusText

    override fun getHeaders(): HttpHeaders = response.headers

    override fun getBody(): InputStream {
        if (body == null) {
            synchronized(this) {
                if (body == null) {
                    body = response.body.readBytes()
                }
            }
        }
        return ByteArrayInputStream(body ?: ByteArray(0))
    }

    override fun close() {
        response.close()
    }
}