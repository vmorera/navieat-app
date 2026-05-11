package com.navieat.app.ui.screens.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navieat.app.domain.model.DietPlan
import com.navieat.app.domain.repository.DietRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    dietRepo: DietRepository,
) : ViewModel() {

    val plan: StateFlow<DietPlan?> = dietRepo.observeActivePlan()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
