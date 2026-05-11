package com.navieat.app.data.repository

import com.navieat.app.data.local.dao.DietPlanDao
import com.navieat.app.data.local.entity.DietPlanEntity
import com.navieat.app.data.local.entity.MealEntity
import com.navieat.app.domain.model.DayPlan
import com.navieat.app.domain.model.DietPlan
import com.navieat.app.domain.model.Dish
import com.navieat.app.domain.model.Meal
import com.navieat.app.domain.model.MealSlot
import com.navieat.app.domain.repository.DietRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DietRepositoryImpl @Inject constructor(
    private val dao: DietPlanDao,
) : DietRepository {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun observeActivePlan(): Flow<DietPlan?> {
        return dao.observeActivePlan().flatMapLatest { plan ->
            if (plan == null) flowOf(null)
            else dao.observeMealsForPlan(plan.id).map { meals -> plan.toDomain(meals) }
        }
    }

    override suspend fun currentMeal(dayOfWeek: Int, hourOfDay: Int): Meal? {
        val planId = activePlanId() ?: return null
        val slot = MealSlot.forHour(hourOfDay)
        return dao.getMeal(planId, dayOfWeek, slot.name)?.let { Meal(it.slot, it.dishes) }
    }

    override suspend fun nextMeal(dayOfWeek: Int, slot: MealSlot): Meal? {
        val planId = activePlanId() ?: return null
        val slots = MealSlot.entries
        val startIdx = slots.indexOf(slot)
        // Walk forward through slots; when we wrap, advance to the next day.
        for (offset in 1..slots.size) {
            val nextIdx = (startIdx + offset) % slots.size
            val nextDay = if (startIdx + offset >= slots.size) ((dayOfWeek % 7) + 1) else dayOfWeek
            val meal = dao.getMeal(planId, nextDay, slots[nextIdx].name)
            if (meal != null) return Meal(meal.slot, meal.dishes)
        }
        return null
    }

    override suspend fun saveActivePlan(plan: DietPlan) {
        val planEntity = DietPlanEntity(
            id = plan.id,
            patientName = plan.patientName,
            createdAtIso = plan.createdAtIso,
            notes = plan.notes,
            isActive = true,
        )
        val mealEntities = plan.days.flatMap { day ->
            day.meals.map { meal ->
                MealEntity(
                    planId = plan.id,
                    dayOfWeek = day.dayOfWeek,
                    slot = meal.slot,
                    dishes = meal.dishes,
                )
            }
        }
        dao.replaceActivePlan(planEntity, mealEntities)
    }

    override suspend fun replaceDish(
        dayOfWeek: Int,
        slot: MealSlot,
        dishId: String,
        replacement: Dish,
    ) {
        val planId = activePlanId() ?: return
        val existing = dao.getMeal(planId, dayOfWeek, slot.name) ?: return
        val updatedDishes = existing.dishes.map { if (it.id == dishId) replacement else it }
        dao.insertMeals(listOf(existing.copy(dishes = updatedDishes)))
    }

    private suspend fun activePlanId(): String? =
        dao.observeActivePlan().first()?.id

    private fun DietPlanEntity.toDomain(meals: List<MealEntity>): DietPlan = DietPlan(
        id = id,
        patientName = patientName,
        createdAtIso = createdAtIso,
        notes = notes,
        days = meals
            .groupBy { it.dayOfWeek }
            .toSortedMap()
            .map { (dow, mealsForDay) ->
                DayPlan(
                    dayOfWeek = dow,
                    meals = mealsForDay
                        .sortedBy { it.slot.ordinal }
                        .map { Meal(slot = it.slot, dishes = it.dishes) },
                )
            },
    )
}
