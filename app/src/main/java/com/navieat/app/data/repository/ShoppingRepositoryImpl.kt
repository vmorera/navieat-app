package com.navieat.app.data.repository

import com.navieat.app.data.local.dao.ShoppingDao
import com.navieat.app.data.local.entity.ShoppingItemEntity
import com.navieat.app.domain.model.ShoppingItem
import com.navieat.app.domain.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingRepositoryImpl @Inject constructor(
    private val dao: ShoppingDao,
) : ShoppingRepository {

    override fun observeAll(): Flow<List<ShoppingItem>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun replaceAll(items: List<ShoppingItem>) {
        dao.clear()
        val now = Instant.now().toString()
        dao.insertAll(items.map { it.toEntity(now) })
    }

    override suspend fun setChecked(id: String, checked: Boolean) =
        dao.setChecked(id, checked)

    override suspend fun remove(id: String) = dao.delete(id)

    override suspend fun clear() = dao.clear()

    private fun ShoppingItemEntity.toDomain() = ShoppingItem(
        id = id,
        name = name,
        quantity = quantity,
        category = category,
        checked = checked,
    )

    private fun ShoppingItem.toEntity(createdAtIso: String) = ShoppingItemEntity(
        id = id,
        name = name,
        quantity = quantity,
        category = category,
        checked = checked,
        createdAtIso = createdAtIso,
    )
}
