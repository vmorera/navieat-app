package com.navieat.app.domain.model

import kotlinx.serialization.Serializable
import java.time.DayOfWeek

/**
 * Structured representation of a dietitian's plan after the LLM has parsed
 * the source PDF. The LLM is instructed to return JSON matching this shape.
 */
@Serializable
data class DietPlan(
    val id: String,
    val patientName: String? = null,
    val createdAtIso: String,
    val notes: String? = null,
    val days: List<DayPlan>
)

@Serializable
data class DayPlan(
    /** ISO day-of-week 1..7 (Mon=1). */
    val dayOfWeek: Int,
    val meals: List<Meal>
) {
    val dayEnum: DayOfWeek get() = DayOfWeek.of(dayOfWeek.coerceIn(1, 7))
}

@Serializable
data class Meal(
    val slot: MealSlot,
    val dishes: List<Dish>
)

@Serializable
data class Dish(
    val id: String,
    val name: String,
    val description: String? = null,
    val ingredients: List<Ingredient> = emptyList(),
    val approxKcal: Int? = null
)

@Serializable
data class Ingredient(
    val name: String,
    val quantity: String? = null
)
