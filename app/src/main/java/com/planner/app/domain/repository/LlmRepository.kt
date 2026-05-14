package com.planner.app.domain.repository

interface LlmRepository {
    suspend fun generateOnDevice(prompt: String): Result<String>
    suspend fun generateCloud(prompt: String): Result<String>
}
