package com.navieat.app.ui.screens.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navieat.app.data.ai.AiException
import com.navieat.app.data.ai.AiProvider
import com.navieat.app.data.pdf.PdfTextExtractor
import com.navieat.app.domain.model.DietPlan
import com.navieat.app.domain.model.Dish
import com.navieat.app.domain.model.Meal
import com.navieat.app.domain.model.MealSlot
import com.navieat.app.domain.repository.DietRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = false,
    val plan: DietPlan? = null,
    val currentMeal: Meal? = null,
    val nextMeal: Meal? = null,
    val currentSlot: MealSlot = MealSlot.BREAKFAST,
    val currentDayOfWeek: Int = 1,
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dietRepo: DietRepository,
    private val ai: AiProvider,
    private val pdfExtractor: PdfTextExtractor,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            dietRepo.observeActivePlan().collect { plan ->
                refresh(plan)
            }
        }
    }

    private suspend fun refresh(plan: DietPlan?) {
        val now = LocalDateTime.now()
        val dow = now.dayOfWeek.value
        val slot = MealSlot.forHour(now.hour)
        val current = dietRepo.currentMeal(dow, now.hour)
        val next = dietRepo.nextMeal(dow, slot)
        _state.update {
            it.copy(
                plan = plan,
                currentMeal = current,
                nextMeal = next,
                currentSlot = slot,
                currentDayOfWeek = dow,
            )
        }
    }

    fun onPdfPicked(uri: Uri, locale: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val text = pdfExtractor.extractText(uri)
                val plan = ai.parseDietFromPdfText(text, locale)
                dietRepo.saveActivePlan(plan)
                _state.update { it.copy(loading = false) }
            } catch (e: AiException) {
                _state.update { it.copy(loading = false, error = e.message) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "PDF parsing failed") }
            }
        }
    }

    fun onSwapDish(dish: Dish, locale: String) {
        val plan = _state.value.plan ?: return
        val slot = _state.value.currentSlot
        val dow = _state.value.currentDayOfWeek
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val replacement = ai.suggestReplacementDish(plan, slot, dish, locale)
                dietRepo.replaceDish(dow, slot, dish.id, replacement)
                _state.update { it.copy(loading = false) }
            } catch (e: AiException) {
                _state.update { it.copy(loading = false, error = e.message) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Swap failed") }
            }
        }
    }

    fun dismissError() = _state.update { it.copy(error = null) }
}
