package com.navieat.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val quantity: String?,
    val category: String?,
    val checked: Boolean,
    val createdAtIso: String,
)
