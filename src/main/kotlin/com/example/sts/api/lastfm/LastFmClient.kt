package com.example.sts.api.lastfm

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class LastFmClient(
    @Qualifier("lastFmRestTemplate")
    private val restTemplate: RestTemplate
) {

    fun getJson(method: String, params: Map<String, String>): JsonNode {
        val uri = UriComponentsBuilder
            .fromPath("")
            .queryParam("method", method)
            .apply {
                params.forEach { (k, v) -> queryParam(k, v) }
            }
            .build()
            .toUriString()

        return restTemplate.getForObject(uri, JsonNode::class.java)
            ?: throw IllegalStateException("Null response from Last.fm")
    }

    fun getTrackTags(artist: String, title: String): MutableList<String> {
        val response = getJson(
            method = "track.getTopTags",
            params = mapOf(
                "artist" to artist,
                "track" to title
            )
        )

        val tags = response["toptags"]?.get("tag")?.mapNotNull { tagNode ->
            tagNode["name"]?.asText()
        } ?: mutableListOf()
        return tags as MutableList<String>
    }

}
