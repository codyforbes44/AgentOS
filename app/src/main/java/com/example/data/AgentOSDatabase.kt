package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AgentEntity::class,
        TaskExecutionEntity::class,
        AnalyticsMetricEntity::class,
        SystemSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AgentOSDatabase : RoomDatabase() {
    abstract fun agentDao(): AgentDao
    abstract fun taskExecutionDao(): TaskExecutionDao
    abstract fun analyticsMetricDao(): AnalyticsMetricDao
    abstract fun systemSettingsDao(): SystemSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AgentOSDatabase? = null

        fun getDatabase(context: Context): AgentOSDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgentOSDatabase::class.java,
                    "agentos_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
