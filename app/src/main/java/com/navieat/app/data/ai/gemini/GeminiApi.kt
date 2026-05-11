package com.navieat.app.data.ai.gemini

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GeminiApi {

    /**
     * model is e.g. "gemini-2.0-flash" or "gemini-2.5-flash". The free tier of
     * AI Studio works with the same endpoint.
     */
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest,
    ): GeminiResponse

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/"
        const val DEFAULT_MODEL = "gemini-2.5-flash"
    }
}
