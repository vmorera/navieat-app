package com.navieat.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingItem(
    val id: String,
    val name: String,
    val quantity: String? = null,
    val category: String? = null,
    val checked: Boolean = false
)
