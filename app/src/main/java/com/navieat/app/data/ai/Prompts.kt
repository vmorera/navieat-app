package com.navieat.app.data.ai

import com.navieat.app.domain.model.DietPlan
import com.navieat.app.domain.model.Dish
import com.navieat.app.domain.model.MealSlot
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Prompt templates. Kept short and explicit; they're shared by all providers.
 * We always force JSON output and validate it by re-parsing into our @Serializable
 * types — if the model deviates from the schema we surface an [AiException].
 */
object Prompts {

    private val json = Json { prettyPrint = false; encodeDefaults = true }

    private const val SCHEMA_DESCRIPTION = """
JSON schema (omit fields you don't have, but stay STRICTLY within this shape):
{
  "id": string,                 // any unique string for the plan
  "patientName": string|null,
  "createdAtIso": string,       // ISO-8601 datetime, e.g. "2026-05-11T00:00:00Z"
  "notes": string|null,         // free-text dietary notes from the dietitian
  "days": [
    {
      "dayOfWeek": int (1=Mon..7=Sun),
      "meals": [
        {
          "slot": "BREAKFAST"|"MID_MORNING"|"LUNCH"|"SNACK"|"DINNER",
          "dishes": [
            {
              "id": string,
              "name": string,
              "description": string|null,
              "ingredients": [ { "name": string, "quantity": string|null } ],
              "approxKcal": int|null
            }
          ]
        }
      ]
    }
  ]
}
"""

    fun parsePdfPrompt(pdfText: String, locale: String): String = """
You are a dietary plan parser. The user has uploaded a PDF written by their dietitian.
Extract the weekly meal plan and output ONLY valid JSON (no markdown, no commentary)
matching the schema below. Use the original language of the PDF for dish names and
descriptions; if the PDF is in Spanish keep them in Spanish. Locale hint: $locale.

$SCHEMA_DESCRIPTION

PDF TEXT (verbatim, may contain noise from OCR):
\"\"\"
$pdfText
\"\"\"
""".trimIndent()

    fun replacementDishPrompt(plan: DietPlan, slot: MealSlot, disliked: Dish, locale: String): String = """
The user dislikes the following dish and wants a replacement that fits the SAME meal
slot of their dietitian's plan. Stay consistent with the macronutrient style and
restrictions inferred from the rest of the plan. Output ONLY valid JSON for a single
Dish object (no array, no commentary):

{ "id": string, "name": string, "description": string|null,
  "ingredients": [ { "name": string, "quantity": string|null } ],
  "approxKcal": int|null }

Locale hint: $locale.
Slot: ${slot.name}
Disliked dish: ${json.encodeToString(disliked)}

Full plan for context:
${json.encodeToString(plan)}
""".trimIndent()

    fun shoppingListPrompt(plan: DietPlan, locale: String): String = """
Aggregate all ingredients from the weekly plan below into a deduplicated shopping
list. Combine quantities of the same ingredient where possible (e.g. "200g chicken"
+ "150g chicken" => "350g chicken"). Assign a coarse category to each item from this
set: PRODUCE, MEAT_FISH, DAIRY, BAKERY, PANTRY, FROZEN, BEVERAGES, OTHER.
Output ONLY a JSON array of objects, no commentary, in the locale "$locale":

[ { "id": string, "name": string, "quantity": string|null, "category": string, "checked": false } ]

Plan:
${json.encodeToString(plan)}
""".trimIndent()
}
