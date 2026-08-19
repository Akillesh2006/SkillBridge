package com.skillbridge.ai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.skillbridge.ai.core.constants.AppConstants

/**
 * Room database for SkillBridge AI.
 *
 * ## Adding Entities
 * 1. Create an `@Entity` class in `data/local/entity/`.
 * 2. Add the class to the [entities] array below.
 * 3. Increment [version] and provide a [androidx.room.migration.Migration].
 *
 * ## Adding DAOs
 * 1. Create a DAO interface in `data/local/dao/`.
 * 2. Declare an abstract accessor function below.
 *
 * @see AppConstants.DATABASE_NAME
 * @see AppConstants.DATABASE_VERSION
 */
@Database(
    entities = [com.skillbridge.ai.data.local.entity.DummyEntity::class],
    version = AppConstants.DATABASE_VERSION,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase()
