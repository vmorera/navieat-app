package com.navieat.app.data.microsoft

import com.navieat.app.data.preferences.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Minimal Microsoft Graph wrapper for the To Do API. We only implement the two
 * calls we need: list the user's task lists (to pick the "Shopping" one) and
 * create a task in it. Full reference:
 *   https://learn.microsoft.com/graph/api/resources/todo-overview
 */
@Singleton
class GraphTodoClient @Inject constructor(
    private val client: OkHttpClient,
    private val msAuth: MicrosoftAuthManager,
    private val prefs: PreferencesRepository,
) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Serializable private data class TaskList(val id: String, val displayName: String)
    @Serializable private data class TaskListsResponse(val value: List<TaskList> = emptyList())
    @Serializable private data class CreateTask(val title: String)

    suspend fun appendToShoppingList(items: List<String>) = withContext(Dispatchers.IO) {
        if (items.isEmpty()) return@withContext
        val token = msAuth.acquireToken()
            ?: error("Not signed in to Microsoft. Sign in from Settings first.")
        val listId = resolveShoppingListId(token)
        items.forEach { name -> createTask(token, listId, name) }
    }

    private suspend fun resolveShoppingListId(token: String): String {
        prefs.microsoftListId.first()?.let { return it }
        val response = client.newCall(
            Request.Builder()
                .url("https://graph.microsoft.com/v1.0/me/todo/lists")
                .header("Authorization", "Bearer $token")
                .build()
        ).execute()
        response.use {
            require(it.isSuccessful) { "Graph error ${it.code}: ${it.message}" }
            val body = it.body?.string().orEmpty()
            val lists = json.decodeFromString<TaskListsResponse>(body).value
            // Prefer a list called "Shopping" / "Compra"; otherwise use the first one.
            val shopping = lists.firstOrNull { l ->
                l.displayName.equals("Shopping", ignoreCase = true) ||
                l.displayName.equals("Compra", ignoreCase = true) ||
                l.displayName.equals("La compra", ignoreCase = true)
            } ?: lists.firstOrNull() ?: error("No To Do lists found in this account.")
            prefs.setMicrosoftListId(shopping.id)
            return shopping.id
        }
    }

    private fun createTask(token: String, listId: String, title: String) {
        val payload = json.encodeToString(CreateTask.serializer(), CreateTask(title))
        val response = client.newCall(
            Request.Builder()
                .url("https://graph.microsoft.com/v1.0/me/todo/lists/$listId/tasks")
                .header("Authorization", "Bearer $token")
                .post(payload.toRequestBody("application/json".toMediaType()))
                .build()
        ).execute()
        response.use {
            require(it.isSuccessful) { "Graph error ${it.code}: ${it.message}" }
        }
    }
}
