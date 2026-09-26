package com.codyforbes.agentos.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Database(
    entities = [
        AgentEntity::class,
        TaskExecutionEntity::class,
        AnalyticsMetricEntity::class,
        SystemSettingsEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AgentOSDatabase : RoomDatabase() {
    abstract fun agentDao(): AgentDao
    abstract fun taskExecutionDao(): TaskExecutionDao
    abstract fun analyticsMetricDao(): AnalyticsMetricDao
    abstract fun systemSettingsDao(): SystemSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AgentOSDatabase? = null

        @Volatile
        private var REPOSITORY: AgentOSRepository? = null

        fun getDatabase(context: Context): AgentOSDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun getRepository(context: Context): AgentOSRepository {
            return REPOSITORY ?: synchronized(this) {
                REPOSITORY ?: AgentOSRepository(
                    db = getDatabase(context),
                    secrets = KeystoreAgentSecrets(context.applicationContext),
                    backgroundScope = repositoryScope(),
                ).also { REPOSITORY = it }
            }
        }

        private fun buildDatabase(context: Context): AgentOSDatabase {
            return Room.databaseBuilder(
                context,
                AgentOSDatabase::class.java,
                "agentos_db",
            )
                .addMigrations(MIGRATION_1_2)
                .build()
        }

        private fun repositoryScope(): CoroutineScope {
            val handler = CoroutineExceptionHandler { _, _ ->
                Log.e("AgentOSRepository", "Background work failed")
            }
            return CoroutineScope(SupervisorJob() + Dispatchers.IO + handler)
        }
    }
}
