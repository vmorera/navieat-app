package com.navieat.app.domain.model

import kotlinx.serialization.Serializable

/**
 * The different meal slots throughout the day.
 * Default time windows are used to decide which meal is "current"
 * given the system clock; they can be overridden by the user later.
 */
@Serializable
enum class MealSlot(val defaultStartHour: Int, val defaultEndHour: Int) {
    BREAKFAST(6, 10),
    MID_MORNING(10, 12),
    LUNCH(12, 16),
    SNACK(16, 19),
    DINNER(19, 23);

    companion object {
        /** Returns the slot whose default window contains [hourOfDay] (0–23). */
        fun forHour(hourOfDay: Int): MealSlot {
            return entries.firstOrNull { hourOfDay in it.defaultStartHour until it.defaultEndHour }
                ?: DINNER
        }
    }
}
