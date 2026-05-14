package com.planner.app.llm.cloud

import com.planner.app.data.local.datastore.PreferencesDataStore
import com.planner.app.domain.repository.LlmRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudLlmService @Inject constructor(
    private val llmRepository: LlmRepository,
    private val prefs: PreferencesDataStore,
) {
    suspend fun generate(prompt: String): Result<String> {
        if (!prefs.llmCloudEnabled.first()) return Result.failure(Exception("Cloud LLM not enabled"))
        return llmRepository.generateCloud(prompt)
    }
}
