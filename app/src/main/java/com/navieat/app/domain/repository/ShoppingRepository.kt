package com.navieat.app.domain.repository

import com.navieat.app.domain.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

interface ShoppingRepository {
    fun observeAll(): Flow<List<ShoppingItem>>
    suspend fun replaceAll(items: List<ShoppingItem>)
    suspend fun setChecked(id: String, checked: Boolean)
    suspend fun remove(id: String)
    suspend fun clear()
}
