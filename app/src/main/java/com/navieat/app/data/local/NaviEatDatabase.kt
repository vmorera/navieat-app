package com.navieat.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.navieat.app.data.local.dao.DietPlanDao
import com.navieat.app.data.local.dao.ShoppingDao
import com.navieat.app.data.local.entity.DietPlanEntity
import com.navieat.app.data.local.entity.MealEntity
import com.navieat.app.data.local.entity.ShoppingItemEntity

@Database(
    entities = [
        DietPlanEntity::class,
        MealEntity::class,
        ShoppingItemEntity::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NaviEatDatabase : RoomDatabase() {
    abstract fun dietPlanDao(): DietPlanDao
    abstract fun shoppingDao(): ShoppingDao

    companion object {
        const val NAME = "navieat.db"
    }
}
