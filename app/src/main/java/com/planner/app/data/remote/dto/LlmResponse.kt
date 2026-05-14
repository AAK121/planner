package com.planner.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnthropicResponse(
    val content: List<AnthropicContent>,
)

@Serializable
data class AnthropicContent(
    val type: String,
    val text: String = "",
)

@Serializable
data class OpenAiResponse(
    val choices: List<OpenAiChoice>,
)

@Serializable
data class OpenAiChoice(
    val message: OpenAiMessage,
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>,
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent,
)

@Serializable
data class GeminiGenerateResponse(
    val candidates: List<GeminiGenerateCandidate>,
)

@Serializable
data class GeminiGenerateCandidate(
    val content: GeminiContent,
    @SerialName("finishReason") val finishReason: String = "",
)
