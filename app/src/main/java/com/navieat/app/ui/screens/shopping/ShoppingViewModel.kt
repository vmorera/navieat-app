package com.navieat.app.ui.screens.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navieat.app.data.ai.AiException
import com.navieat.app.data.ai.AiProvider
import com.navieat.app.data.microsoft.GraphTodoClient
import com.navieat.app.domain.model.ShoppingItem
import com.navieat.app.domain.repository.DietRepository
import com.navieat.app.domain.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShoppingUiState(
    val loading: Boolean = false,
    val items: List<ShoppingItem> = emptyList(),
    val error: String? = null,
    val info: String? = null,
)

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val shoppingRepo: ShoppingRepository,
    private val dietRepo: DietRepository,
    private val ai: AiProvider,
    private val msTodo: GraphTodoClient,
) : ViewModel() {

    private val _state = MutableStateFlow(ShoppingUiState())
    val state: StateFlow<ShoppingUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            shoppingRepo.observeAll().collect { items ->
                _state.update { it.copy(items = items) }
            }
        }
    }

    fun generateFromPlan(locale: String) {
        viewModelScope.launch {
            val plan = dietRepo.observeActivePlan().first()
            if (plan == null) {
                _state.update { it.copy(error = "No active plan to generate from") }
                return@launch
            }
            _state.update { it.copy(loading = true, error = null) }
            try {
                val items = ai.buildShoppingList(plan, locale)
                shoppingRepo.replaceAll(items)
                _state.update { it.copy(loading = false) }
            } catch (e: AiException) {
                _state.update { it.copy(loading = false, error = e.message) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Failed") }
            }
        }
    }

    fun toggle(id: String, checked: Boolean) = viewModelScope.launch {
        shoppingRepo.setChecked(id, checked)
    }

    fun sendToMicrosoftTodo() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val items = _state.value.items.filter { !it.checked }
                msTodo.appendToShoppingList(items.map { it.name })
                _state.update { it.copy(loading = false, info = "Sent to Microsoft To Do") }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "MS To Do failed") }
            }
        }
    }

    fun dismissError() = _state.update { it.copy(error = null, info = null) }
}
