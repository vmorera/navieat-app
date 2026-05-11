package com.navieat.app.di

import com.navieat.app.data.ai.AiProvider
import com.navieat.app.data.ai.gemini.GeminiProvider
import com.navieat.app.data.repository.DietRepositoryImpl
import com.navieat.app.data.repository.ShoppingRepositoryImpl
import com.navieat.app.domain.repository.DietRepository
import com.navieat.app.domain.repository.ShoppingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Interface bindings. PreferencesRepository is auto-provided via its
 * @Singleton @Inject constructor, no module entry needed.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindings {

    @Binds
    @Singleton
    abstract fun bindDietRepository(impl: DietRepositoryImpl): DietRepository

    @Binds
    @Singleton
    abstract fun bindShoppingRepository(impl: ShoppingRepositoryImpl): ShoppingRepository

    /**
     * Default AI provider is Gemini. To use a different provider, change this
     * binding (or inject the user-selected one from PreferencesRepository).
     */
    @Binds
    @Singleton
    abstract fun bindAiProvider(impl: GeminiProvider): AiProvider
}
