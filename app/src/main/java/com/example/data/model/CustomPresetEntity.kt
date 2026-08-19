package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_equalizer_presets")
data class CustomPresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val band0: Int = 0,
    val band1: Int = 0,
    val band2: Int = 0,
    val band3: Int = 0,
    val band4: Int = 0,
    val bassBoost: Int = 0,
    val virtualizer: Int = 0
)
