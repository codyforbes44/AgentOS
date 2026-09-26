package com.codyforbes.agentos.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentDao {
    @Query("SELECT * FROM agents ORDER BY name ASC")
    fun getAllAgents(): Flow<List<AgentEntity>>

    @Query("SELECT * FROM agents WHERE id = :id")
    suspend fun getAgentById(id: String): AgentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: AgentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgents(agents: List<AgentEntity>)

    @Update
    suspend fun updateAgent(agent: AgentEntity)

    @Query("DELETE FROM agents WHERE id = :id")
    suspend fun deleteAgent(id: String)

    @Query("UPDATE agents SET status = :status WHERE id = :id")
    suspend fun updateAgentStatus(id: String, status: String)

    @Query("UPDATE agents SET status = 'OFFLINE' WHERE status = 'EXECUTING'")
    suspend fun stopAllExecutingAgents()
}

@Dao
interface TaskExecutionDao {
    @Query("SELECT * FROM task_executions ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskExecutionEntity>>

    @Query("SELECT * FROM task_executions WHERE id = :id")
    suspend fun getTaskById(id: String): TaskExecutionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskExecutionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskExecutionEntity>)

    @Update
    suspend fun updateTask(task: TaskExecutionEntity)

    @Query("UPDATE task_executions SET status = 'KILLED', logsText = logsText || '\n[EMERGENCY KILL SWITCH ENGAGED - EXECUTION HALTED]' WHERE status IN ('RUNNING', 'AWAITING_HITL', 'QUEUED')")
    suspend fun killAllActiveTasks()

    @Query("DELETE FROM task_executions WHERE id = :id")
    suspend fun deleteTask(id: String)
}

@Dao
interface AnalyticsMetricDao {
    @Query("SELECT * FROM analytics_metrics ORDER BY timestamp DESC")
    fun getAllMetrics(): Flow<List<AnalyticsMetricEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetric(metric: AnalyticsMetricEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetrics(metrics: List<AnalyticsMetricEntity>)
}

@Dao
interface SystemSettingsDao {
    @Query("SELECT * FROM system_settings WHERE id = 'global_settings'")
    fun getSettings(): Flow<SystemSettingsEntity?>

    @Query("SELECT * FROM system_settings WHERE id = 'global_settings'")
    suspend fun getSettingsDirect(): SystemSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SystemSettingsEntity)
}
