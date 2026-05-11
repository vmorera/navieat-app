package com.navieat.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.navieat.app.data.local.entity.DietPlanEntity
import com.navieat.app.data.local.entity.MealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DietPlanDao {

    @Query("SELECT * FROM diet_plans WHERE isActive = 1 LIMIT 1")
    fun observeActivePlan(): Flow<DietPlanEntity?>

    @Query("SELECT * FROM meals WHERE planId = :planId ORDER BY dayOfWeek, slot")
    fun observeMealsForPlan(planId: String): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE planId = :planId AND dayOfWeek = :dayOfWeek AND slot = :slot LIMIT 1")
    suspend fun getMeal(planId: String, dayOfWeek: Int, slot: String): MealEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: DietPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<MealEntity>)

    @Query("UPDATE diet_plans SET isActive = 0")
    suspend fun deactivateAll()

    @Query("DELETE FROM meals WHERE planId = :planId")
    suspend fun deleteMealsForPlan(planId: String)

    @Transaction
    suspend fun replaceActivePlan(plan: DietPlanEntity, meals: List<MealEntity>) {
        deactivateAll()
        insertPlan(plan.copy(isActive = true))
        deleteMealsForPlan(plan.id)
        insertMeals(meals)
    }
}
