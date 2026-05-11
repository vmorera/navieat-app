package com.navieat.app.data.ai

import com.navieat.app.domain.model.DietPlan
import com.navieat.app.domain.model.Dish
import com.navieat.app.domain.model.MealSlot
import com.navieat.app.domain.model.ShoppingItem

/**
 * Abstraction over the LLM backend. Lets us swap Gemini for OpenAI, Bedrock+Kimi,
 * Claude, etc. without touching the rest of the app.
 *
 * All implementations MUST be safe to call from a coroutine context (network IO),
 * and SHOULD throw [AiException] with a meaningful message on failure so the UI
 * can show it.
 */
interface AiProvider {

    /** Stable id used in settings / logs. */
    val id: String

    /**
     * Given the raw text extracted from the dietitian's PDF, return a structured
     * [DietPlan]. The implementation prompts the model to output JSON matching
     * the [DietPlan] schema.
     */
    suspend fun parseDietFromPdfText(pdfText: String, locale: String): DietPlan

    /**
     * Suggest a replacement dish that fits the same slot of the diet plan, using
     * the existing plan as context so it stays within the dietitian's
     * macronutrient/style constraints.
     */
    suspend fun suggestReplacementDish(
        plan: DietPlan,
        slot: MealSlot,
        disliked: Dish,
        locale: String,
    ): Dish

    /**
     * Aggregate the dishes of the plan into a deduplicated shopping list.
     */
    suspend fun buildShoppingList(plan: DietPlan, locale: String): List<ShoppingItem>
}

class AiException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
