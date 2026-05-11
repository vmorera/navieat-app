package com.navieat.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.navieat.app.domain.model.Dish
import com.navieat.app.domain.model.MealSlot

@Entity(
    tableName = "meals",
    foreignKeys = [
        ForeignKey(
            entity = DietPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId"), Index(value = ["planId", "dayOfWeek", "slot"], unique = true)]
)
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val planId: String,
    /** ISO day-of-week 1..7 (Mon=1). */
    val dayOfWeek: Int,
    val slot: MealSlot,
    /** Stored as JSON via TypeConverter. */
    val dishes: List<Dish>
)
