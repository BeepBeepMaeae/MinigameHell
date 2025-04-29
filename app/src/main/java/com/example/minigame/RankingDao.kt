package com.example.minigame

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RankingDao {

    @Insert
    suspend fun insertRanking(ranking: RankingEntity)

    @Query("SELECT * FROM ranking_table WHERE gameType = :game ORDER BY score DESC LIMIT 5")
    suspend fun getTop5Rankings(game: String): List<RankingEntity>
}