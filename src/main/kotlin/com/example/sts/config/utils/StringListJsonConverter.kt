package com.example.sts.config.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringListJsonConverter : AttributeConverter<List<String>, String> {
    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<String>?): String {
        return objectMapper.writeValueAsString(attribute ?: listOf(""))
    }

    override fun convertToEntityAttribute(dbData: String?): List<String> {
        return dbData?.let {
            objectMapper.readValue(it, object : TypeReference<List<String>>() {})
        } ?: emptyList()
    }
}
