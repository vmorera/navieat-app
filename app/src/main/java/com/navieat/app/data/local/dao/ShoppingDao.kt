package com.navieat.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.navieat.app.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {

    @Query("SELECT * FROM shopping_items ORDER BY checked ASC, createdAtIso DESC")
    fun observeAll(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShoppingItemEntity>)

    @Query("UPDATE shopping_items SET checked = :checked WHERE id = :id")
    suspend fun setChecked(id: String, checked: Boolean)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM shopping_items")
    suspend fun clear()
}
