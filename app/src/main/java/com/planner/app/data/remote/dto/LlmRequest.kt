package com.planner.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnthropicRequest(
    val model: String = "claude-haiku-4-5-20251001",
    @SerialName("max_tokens") val maxTokens: Int = 1024,
    val messages: List<AnthropicMessage>,
)

@Serializable
data class AnthropicMessage(
    val role: String,
    val content: String,
)

@Serializable
data class OpenAiRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<OpenAiMessage>,
    @SerialName("max_tokens") val maxTokens: Int = 1024,
)

@Serializable
data class OpenAiMessage(
    val role: String,
    val content: String,
)

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>,
)

@Serializable
data class GeminiPart(
    val text: String,
)
