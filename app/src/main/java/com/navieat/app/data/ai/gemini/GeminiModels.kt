package com.navieat.app.data.ai.gemini

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Minimal subset of the Gemini generateContent request/response. The full schema
 * is documented at https://ai.google.dev/api/rest/v1beta/models/generateContent.
 */
@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null,
)

@Serializable
data class Content(
    val role: String? = null,
    val parts: List<Part>,
)

@Serializable
data class Part(
    val text: String? = null,
)

@Serializable
data class GenerationConfig(
    val temperature: Double? = 0.2,
    @SerialName("maxOutputTokens") val maxOutputTokens: Int? = 4096,
    @SerialName("responseMimeType") val responseMimeType: String? = "application/json",
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate> = emptyList(),
    val promptFeedback: PromptFeedback? = null,
)

@Serializable
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null,
)

@Serializable
data class PromptFeedback(
    val blockReason: String? = null,
)
