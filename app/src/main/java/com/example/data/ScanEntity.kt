package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val room: String,
    val winPercentage: Double,
    val timestamp: Long = System.currentTimeMillis()
)
