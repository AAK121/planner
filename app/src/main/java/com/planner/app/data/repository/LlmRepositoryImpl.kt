package com.planner.app.data.repository

import com.planner.app.data.local.datastore.PreferencesDataStore
import com.planner.app.data.remote.api.AnthropicApi
import com.planner.app.data.remote.api.GeminiApi
import com.planner.app.data.remote.api.OpenAiApi
import com.planner.app.data.remote.dto.AnthropicMessage
import com.planner.app.data.remote.dto.AnthropicRequest
import com.planner.app.data.remote.dto.GeminiContent
import com.planner.app.data.remote.dto.GeminiPart
import com.planner.app.data.remote.dto.GeminiRequest
import com.planner.app.data.remote.dto.OpenAiMessage
import com.planner.app.data.remote.dto.OpenAiRequest
import com.planner.app.domain.repository.LlmRepository
import com.planner.app.llm.ondevice.OnDeviceLlmManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LlmRepositoryImpl @Inject constructor(
    private val prefs: PreferencesDataStore,
    private val anthropicApi: AnthropicApi,
    private val openAiApi: OpenAiApi,
    private val geminiApi: GeminiApi,
    private val onDeviceManager: OnDeviceLlmManager,
) : LlmRepository {

    override suspend fun generateOnDevice(prompt: String): Result<String> =
        onDeviceManager.generate(prompt)

    override suspend fun generateCloud(prompt: String): Result<String> = runCatching {
        val provider = prefs.llmProvider.first()
        val apiKey = prefs.llmApiKey.first()
        when (provider) {
            "anthropic" -> {
                val response = anthropicApi.sendMessage(
                    apiKey = apiKey,
                    request = AnthropicRequest(
                        messages = listOf(AnthropicMessage(role = "user", content = prompt))
                    )
                )
                response.content.firstOrNull { it.type == "text" }?.text ?: ""
            }
            "openai" -> {
                val response = openAiApi.complete(
                    bearer = "Bearer $apiKey",
                    request = OpenAiRequest(
                        messages = listOf(OpenAiMessage(role = "user", content = prompt))
                    )
                )
                response.choices.firstOrNull()?.message?.content ?: ""
            }
            "gemini" -> {
                val response = geminiApi.generate(
                    apiKey = apiKey,
                    request = GeminiRequest(
                        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
                    )
                )
                response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            }
            else -> throw IllegalArgumentException("Unknown LLM provider: $provider")
        }
    }
}
