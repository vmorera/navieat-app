package com.navieat.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diet_plans")
data class DietPlanEntity(
    @PrimaryKey val id: String,
    val patientName: String?,
    val createdAtIso: String,
    val notes: String?,
    val isActive: Boolean = true,
)
