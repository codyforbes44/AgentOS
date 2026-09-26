package com.codyforbes.agentos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agents")
data class AgentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val provider: String, // OpenAI, Anthropic, LangGraph, Custom REST, Google Gemini
    val endpointUrl: String,
    val maskedApiKey: String,
    val status: String, // ONLINE_IDLE, EXECUTING, AWAITING_APPROVAL, RATE_LIMITED, ERROR, OFFLINE
    val tags: String, // Comma separated: e.g. "Finance,PDF,SQL"
    val maxCostCapUSD: Double,
    val timeoutSeconds: Int,
    val hitlEnabled: Boolean,
    val healthScore: Int, // 0 - 100
    val dailyTokenBurn: Long,
    val totalCostUSD: Double,
    val capabilities: String, // Comma separated: e.g. "Web Search, Code Exec, SQL Access"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "task_executions")
data class TaskExecutionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val agentId: String,
    val agentName: String,
    val flowType: String, // SINGLE, SEQUENTIAL, PARALLEL_DAG
    val status: String, // QUEUED, RUNNING, AWAITING_HITL, COMPLETED, FAILED, KILLED
    val objective: String,
    val parametersJson: String,
    val currentStepIndex: Int,
    val totalSteps: Int,
    val estimatedCostUSD: Double,
    val actualCostUSD: Double,
    val tokensInput: Long,
    val tokensOutput: Long,
    val tokensReasoning: Long,
    val logsText: String,
    val hitlReason: String, // Reason if status is AWAITING_HITL
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "analytics_metrics")
data class AnalyticsMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val agentId: String,
    val agentName: String,
    val tokensInput: Long,
    val tokensOutput: Long,
    val tokensReasoning: Long,
    val costUSD: Double,
    val latencyMs: Long,
    val isSuccess: Boolean
)

@Entity(tableName = "system_settings")
data class SystemSettingsEntity(
    @PrimaryKey val id: String = "global_settings",
    val dailyCostCapUSD: Double = 50.0,
    val monthlyCostCapUSD: Double = 1000.0,
    val circuitBreakerEnabled: Boolean = true,
    val emergencyKillSwitchEngaged: Boolean = false,
    val activeRole: String = "Admin", // Admin, Operator, Viewer
    val pushNotificationsEnabled: Boolean = true,
    val hitlGateForMutations: Boolean = true,
    val hitlGateForShell: Boolean = true
)
