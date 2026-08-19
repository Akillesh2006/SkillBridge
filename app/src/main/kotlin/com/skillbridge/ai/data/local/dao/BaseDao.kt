package com.skillbridge.ai.data.local.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

/**
 * Generic base DAO providing common CRUD operations.
 *
 * Concrete DAOs should extend this interface and add query-specific methods.
 *
 * @param T The Room entity type managed by this DAO.
 */
interface BaseDao<T> {

    /**
     * Inserts a single entity. On conflict, the existing row is replaced.
     * @return The row ID of the inserted entity.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: T): Long

    /**
     * Inserts a list of entities. On conflict, existing rows are replaced.
     * @return A list of row IDs for all inserted entities.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<T>): List<Long>

    /**
     * Updates an existing entity matched by its primary key.
     * @return The number of rows updated.
     */
    @Update
    suspend fun update(entity: T): Int

    /**
     * Deletes a single entity matched by its primary key.
     * @return The number of rows deleted.
     */
    @Delete
    suspend fun delete(entity: T): Int
}
