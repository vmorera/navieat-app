package com.navieat.app.di

import android.content.Context
import androidx.room.Room
import com.navieat.app.data.local.NaviEatDatabase
import com.navieat.app.data.local.dao.DietPlanDao
import com.navieat.app.data.local.dao.ShoppingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NaviEatDatabase =
        Room.databaseBuilder(context, NaviEatDatabase::class.java, NaviEatDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDietPlanDao(db: NaviEatDatabase): DietPlanDao = db.dietPlanDao()

    @Provides
    fun provideShoppingDao(db: NaviEatDatabase): ShoppingDao = db.shoppingDao()
}
