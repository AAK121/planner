package com.planner.app.data.remote.api

import com.planner.app.data.remote.dto.AnthropicRequest
import com.planner.app.data.remote.dto.AnthropicResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AnthropicApi {
    @POST("v1/messages")
    suspend fun sendMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: AnthropicRequest,
    ): AnthropicResponse
}
