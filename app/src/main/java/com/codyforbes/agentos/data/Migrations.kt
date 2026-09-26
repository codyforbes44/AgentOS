package com.codyforbes.agentos.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_agents_status` ON `agents` (`status`)")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_task_executions_agentId` ON `task_executions` (`agentId`)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_task_executions_status` ON `task_executions` (`status`)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_analytics_metrics_timestamp` ON `analytics_metrics` (`timestamp`)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_analytics_metrics_agentId` ON `analytics_metrics` (`agentId`)",
        )
    }
}
