package com.navieat.app.domain.repository

import com.navieat.app.domain.model.DietPlan
import com.navieat.app.domain.model.Dish
import com.navieat.app.domain.model.Meal
import com.navieat.app.domain.model.MealSlot
import kotlinx.coroutines.flow.Flow

interface DietRepository {

    /** Currently active plan (the latest one uploaded). */
    fun observeActivePlan(): Flow<DietPlan?>

    /** What the user should be eating right now according to the active plan. */
    suspend fun currentMeal(dayOfWeek: Int, hourOfDay: Int): Meal?

    /** The meal immediately after [slot] on [dayOfWeek] (wraps to next day). */
    suspend fun nextMeal(dayOfWeek: Int, slot: MealSlot): Meal?

    /** Persist a freshly-parsed plan as the active one. */
    suspend fun saveActivePlan(plan: DietPlan)

    /** Replace a single dish (used for the "I don't feel like this" flow). */
    suspend fun replaceDish(dayOfWeek: Int, slot: MealSlot, dishId: String, replacement: Dish)
}
