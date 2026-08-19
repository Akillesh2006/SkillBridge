package com.skillbridge.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dummy_table")
data class DummyEntity(
    @PrimaryKey
    val id: Int = 1,
    val value: String = ""
)
