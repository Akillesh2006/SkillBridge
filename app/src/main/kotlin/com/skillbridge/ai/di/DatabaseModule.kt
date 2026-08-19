package com.skillbridge.ai.di

import android.content.Context
import androidx.room.Room
import com.skillbridge.ai.core.constants.AppConstants
import com.skillbridge.ai.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides the Room database and its DAOs.
 *
 * ## Adding a new DAO
 * 1. Create the DAO interface in `data/local/dao/`.
 * 2. Declare an abstract accessor in [AppDatabase].
 * 3. Add a `@Provides` function here delegating to `database.yourDao()`.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        AppConstants.DATABASE_NAME,
    )
        .fallbackToDestructiveMigration()
        .build()

    // ── DAO providers will be added here as entities are introduced ────────────
}
