package com.navieat.app.data.local

import androidx.room.TypeConverter
import com.navieat.app.domain.model.Dish
import com.navieat.app.domain.model.Ingredient
import com.navieat.app.domain.model.MealSlot
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room type converters for the JSON columns we store inline. We keep the
 * schema flat: one row per day-meal with the list of dishes serialized.
 */
class Converters {

    @TypeConverter
    fun fromMealSlot(slot: MealSlot): String = slot.name

    @TypeConverter
    fun toMealSlot(value: String): MealSlot = MealSlot.valueOf(value)

    @TypeConverter
    fun fromDishList(dishes: List<Dish>): String = json.encodeToString(dishes)

    @TypeConverter
    fun toDishList(value: String): List<Dish> = json.decodeFromString(value)

    @TypeConverter
    fun fromIngredientList(ings: List<Ingredient>): String = json.encodeToString(ings)

    @TypeConverter
    fun toIngredientList(value: String): List<Ingredient> = json.decodeFromString(value)

    companion object {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}
