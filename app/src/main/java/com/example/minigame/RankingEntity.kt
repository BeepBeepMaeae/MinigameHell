package com.example.minigame

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ranking_table")
data class RankingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameType: String,
    val nickname: String,
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
)