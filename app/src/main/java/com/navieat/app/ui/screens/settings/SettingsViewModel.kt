package com.navieat.app.ui.screens.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navieat.app.data.microsoft.MicrosoftAuthManager
import com.navieat.app.data.preferences.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val provider: String = PreferencesRepository.PROVIDER_GEMINI,
    val geminiApiKey: String = "",
    val microsoftAccount: String? = null,
    val saving: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesRepository,
    private val msAuth: MicrosoftAuthManager,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.aiProvider.collect { p -> _state.update { it.copy(provider = p) } }
        }
        viewModelScope.launch {
            prefs.geminiApiKey.collect { k -> _state.update { it.copy(geminiApiKey = k.orEmpty()) } }
        }
        viewModelScope.launch {
            _state.update { it.copy(microsoftAccount = msAuth.currentAccount()) }
        }
    }

    fun onProviderChanged(provider: String) = viewModelScope.launch {
        prefs.setAiProvider(provider)
    }

    fun onGeminiKeyChanged(key: String) = viewModelScope.launch {
        prefs.setGeminiApiKey(key)
    }

    fun signInMicrosoft(activity: Activity) = viewModelScope.launch {
        _state.update { it.copy(saving = true) }
        val account = msAuth.signIn(activity)
        _state.update { it.copy(saving = false, microsoftAccount = account) }
    }

    fun signOutMicrosoft() = viewModelScope.launch {
        msAuth.signOut()
        _state.update { it.copy(microsoftAccount = null) }
    }
}
