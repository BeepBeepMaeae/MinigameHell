package com.example.minigame

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RankingEntity::class], version = 1)
abstract class RankingDatabase : RoomDatabase() {
    abstract fun rankingDao(): RankingDao

    companion object {
        @Volatile
        private var INSTANCE: RankingDatabase? = null

        fun getDatabase(context: Context): RankingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RankingDatabase::class.java,
                    "ranking_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}