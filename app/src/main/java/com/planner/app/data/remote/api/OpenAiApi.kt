package com.planner.app.data.remote.api

import com.planner.app.data.remote.dto.OpenAiRequest
import com.planner.app.data.remote.dto.OpenAiResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAiApi {
    @POST("v1/chat/completions")
    suspend fun complete(
        @Header("Authorization") bearer: String,
        @Body request: OpenAiRequest,
    ): OpenAiResponse
}
