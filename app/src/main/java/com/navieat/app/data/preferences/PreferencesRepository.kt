package com.navieat.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "navieat_prefs")

/**
 * User preferences and credentials. API keys are stored here for simplicity;
 * a future iteration can move them into the Android Keystore for hardware-backed
 * encryption.
 */
@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val aiProvider: Flow<String> = context.dataStore.data.map { it[KEY_AI_PROVIDER] ?: DEFAULT_PROVIDER }
    val geminiApiKey: Flow<String?> = context.dataStore.data.map { it[KEY_GEMINI_API_KEY] }
    val openAiApiKey: Flow<String?> = context.dataStore.data.map { it[KEY_OPENAI_API_KEY] }
    val bedrockAccessKey: Flow<String?> = context.dataStore.data.map { it[KEY_BEDROCK_ACCESS_KEY] }
    val bedrockSecretKey: Flow<String?> = context.dataStore.data.map { it[KEY_BEDROCK_SECRET_KEY] }
    val bedrockRegion: Flow<String?> = context.dataStore.data.map { it[KEY_BEDROCK_REGION] }
    val microsoftListId: Flow<String?> = context.dataStore.data.map { it[KEY_MS_LIST_ID] }

    suspend fun setAiProvider(value: String) =
        context.dataStore.edit { it[KEY_AI_PROVIDER] = value }

    suspend fun setGeminiApiKey(value: String) =
        context.dataStore.edit { it[KEY_GEMINI_API_KEY] = value }

    suspend fun setOpenAiApiKey(value: String) =
        context.dataStore.edit { it[KEY_OPENAI_API_KEY] = value }

    suspend fun setBedrockCredentials(accessKey: String, secretKey: String, region: String) =
        context.dataStore.edit {
            it[KEY_BEDROCK_ACCESS_KEY] = accessKey
            it[KEY_BEDROCK_SECRET_KEY] = secretKey
            it[KEY_BEDROCK_REGION] = region
        }

    suspend fun setMicrosoftListId(value: String?) =
        context.dataStore.edit {
            if (value == null) it.remove(KEY_MS_LIST_ID) else it[KEY_MS_LIST_ID] = value
        }

    companion object {
        const val PROVIDER_GEMINI = "gemini"
        const val PROVIDER_OPENAI = "openai"
        const val PROVIDER_BEDROCK = "bedrock"
        private const val DEFAULT_PROVIDER = PROVIDER_GEMINI

        private val KEY_AI_PROVIDER: Preferences.Key<String> = stringPreferencesKey("ai_provider")
        private val KEY_GEMINI_API_KEY: Preferences.Key<String> = stringPreferencesKey("gemini_api_key")
        private val KEY_OPENAI_API_KEY: Preferences.Key<String> = stringPreferencesKey("openai_api_key")
        private val KEY_BEDROCK_ACCESS_KEY: Preferences.Key<String> = stringPreferencesKey("bedrock_access_key")
        private val KEY_BEDROCK_SECRET_KEY: Preferences.Key<String> = stringPreferencesKey("bedrock_secret_key")
        private val KEY_BEDROCK_REGION: Preferences.Key<String> = stringPreferencesKey("bedrock_region")
        private val KEY_MS_LIST_ID: Preferences.Key<String> = stringPreferencesKey("ms_list_id")
    }
}
