package com.navieat.app.data.ai.gemini

import com.navieat.app.data.ai.AiException
import com.navieat.app.data.ai.AiProvider
import com.navieat.app.data.ai.Prompts
import com.navieat.app.data.preferences.PreferencesRepository
import com.navieat.app.domain.model.DietPlan
import com.navieat.app.domain.model.Dish
import com.navieat.app.domain.model.MealSlot
import com.navieat.app.domain.model.ShoppingItem
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiProvider @Inject constructor(
    private val api: GeminiApi,
    private val prefs: PreferencesRepository,
) : AiProvider {

    override val id: String = PreferencesRepository.PROVIDER_GEMINI

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }

    override suspend fun parseDietFromPdfText(pdfText: String, locale: String): DietPlan {
        val prompt = Prompts.parsePdfPrompt(pdfText, locale)
        val raw = generate(prompt)
        return runCatching { json.decodeFromString<DietPlan>(raw) }
            .getOrElse { throw AiException("Gemini returned malformed JSON for diet plan: ${it.message}", it) }
    }

    override suspend fun suggestReplacementDish(
        plan: DietPlan,
        slot: MealSlot,
        disliked: Dish,
        locale: String,
    ): Dish {
        val prompt = Prompts.replacementDishPrompt(plan, slot, disliked, locale)
        val raw = generate(prompt)
        return runCatching { json.decodeFromString<Dish>(raw) }
            .getOrElse { throw AiException("Gemini returned malformed JSON for replacement dish: ${it.message}", it) }
    }

    override suspend fun buildShoppingList(plan: DietPlan, locale: String): List<ShoppingItem> {
        val prompt = Prompts.shoppingListPrompt(plan, locale)
        val raw = generate(prompt)
        return runCatching { json.decodeFromString<List<ShoppingItem>>(raw) }
            .getOrElse { throw AiException("Gemini returned malformed JSON for shopping list: ${it.message}", it) }
    }

    private suspend fun generate(prompt: String): String {
        val key = prefs.geminiApiKey.first()
            ?: throw AiException("Missing Gemini API key. Set it in Settings.")
        val response = api.generateContent(
            model = GeminiApi.DEFAULT_MODEL,
            apiKey = key,
            request = GeminiRequest(
                contents = listOf(Content(role = "user", parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig()
            )
        )
        response.promptFeedback?.blockReason?.let { reason ->
            throw AiException("Gemini blocked the request: $reason")
        }
        val text = response.candidates.firstOrNull()
            ?.content?.parts?.mapNotNull { it.text }?.joinToString("")
            ?: throw AiException("Gemini returned no text in response")
        return text.trim().removeSurrounding("```json", "```").removeSurrounding("```", "```").trim()
    }
}
