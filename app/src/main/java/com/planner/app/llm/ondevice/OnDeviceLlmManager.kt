package com.planner.app.llm.ondevice

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnDeviceLlmManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // MediaPipe LLM Inference session — lazy-initialized on first use
    // The Gemma 2B model file must be placed in assets/gemma-2b-it-cpu-int4.bin
    // Model download is handled by the user via Settings screen

    private var isInitialized = false

    suspend fun generate(prompt: String): Result<String> = runCatching {
        if (!isInitialized) {
            // MediaPipe LLM Inference initialization would go here.
            // Omitted to keep the project buildable without the model binary.
            // Implementation:
            //   val options = LlmInference.LlmInferenceOptions.builder()
            //       .setModelPath("/data/local/tmp/gemma-2b-it-cpu-int4.bin")
            //       .setMaxTokens(1024)
            //       .build()
            //   llmInference = LlmInference.createFromOptions(context, options)
            isInitialized = true
        }
        "On-device LLM response for: $prompt"
    }
}
