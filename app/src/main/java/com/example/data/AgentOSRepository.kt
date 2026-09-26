package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

class AgentOSRepository(
    private val db: AgentOSDatabase,
    private val scope: CoroutineScope
) {
    val agents: Flow<List<AgentEntity>> = db.agentDao().getAllAgents()
    val tasks: Flow<List<TaskExecutionEntity>> = db.taskExecutionDao().getAllTasks()
    val metrics: Flow<List<AnalyticsMetricEntity>> = db.analyticsMetricDao().getAllMetrics()
    val settings: Flow<SystemSettingsEntity?> = db.systemSettingsDao().getSettings()

    init {
        scope.launch(Dispatchers.IO) {
            seedInitialDataIfNeeded()
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        val currentAgents = db.agentDao().getAllAgents().firstOrNull()
        if (currentAgents.isNullOrEmpty()) {
            val initialAgents = listOf(
                AgentEntity(
                    id = "ag_finance_v2",
                    name = "FinanceBot-v2",
                    provider = "Anthropic",
                    endpointUrl = "https://api.anthropic.com/v1/messages",
                    maskedApiKey = "sk-ant-api03-••••••••••••39a1",
                    status = "AWAITING_APPROVAL",
                    tags = "Finance,Audit,PDF",
                    maxCostCapUSD = 1.50,
                    timeoutSeconds = 120,
                    hitlEnabled = true,
                    healthScore = 98,
                    dailyTokenBurn = 412500,
                    totalCostUSD = 8.25,
                    capabilities = "Code Exec, Web Search, Document Parsing, SQL Write"
                ),
                AgentEntity(
                    id = "ag_data_extract",
                    name = "DataExtractionAgent",
                    provider = "OpenAI",
                    endpointUrl = "https://api.openai.com/v1/chat/completions",
                    maskedApiKey = "sk-proj-••••••••••••811f",
                    status = "EXECUTING",
                    tags = "ETL,JSON,DB",
                    maxCostCapUSD = 2.00,
                    timeoutSeconds = 180,
                    hitlEnabled = true,
                    healthScore = 94,
                    dailyTokenBurn = 780000,
                    totalCostUSD = 14.60,
                    capabilities = "SQL Read/Write, API Fetch, JSON Transformation"
                ),
                AgentEntity(
                    id = "ag_langgraph_flow",
                    name = "CustomerSupportDAG",
                    provider = "LangGraph",
                    endpointUrl = "https://agentos.internal/langgraph/support-v1",
                    maskedApiKey = "Bearer lg_sec_••••••••4f2b",
                    status = "ONLINE_IDLE",
                    tags = "Support,Multi-Agent,Workflow",
                    maxCostCapUSD = 5.00,
                    timeoutSeconds = 300,
                    hitlEnabled = false,
                    healthScore = 100,
                    dailyTokenBurn = 120000,
                    totalCostUSD = 2.40,
                    capabilities = "CRM Lookup, Ticket Escalation, Email Synthesis"
                ),
                AgentEntity(
                    id = "ag_custom_rest",
                    name = "SecurityScannerAgent",
                    provider = "Custom REST",
                    endpointUrl = "https://sec.internal.net/v2/scan",
                    maskedApiKey = "X-Api-Key sec_••••••••90aa",
                    status = "RATE_LIMITED",
                    tags = "Security,Audit,Shell",
                    maxCostCapUSD = 1.00,
                    timeoutSeconds = 60,
                    hitlEnabled = true,
                    healthScore = 82,
                    dailyTokenBurn = 290000,
                    totalCostUSD = 5.10,
                    capabilities = "Shell Exec, Port Scan, Vulnerability DB"
                )
            )
            db.agentDao().insertAgents(initialAgents)
        }

        val currentTasks = db.taskExecutionDao().getAllTasks().firstOrNull()
        if (currentTasks.isNullOrEmpty()) {
            val initialTasks = listOf(
                TaskExecutionEntity(
                    id = "task_8402",
                    title = "Audit Q3 Financial Statements & DB Update",
                    agentId = "ag_finance_v2",
                    agentName = "FinanceBot-v2",
                    flowType = "SINGLE",
                    status = "AWAITING_HITL",
                    objective = "Summarize Q3 Financial Report PDF and update 14 records in [Users_DB] with adjusted revenue credits.",
                    parametersJson = """{"max_cost_cap": 0.50, "timeout": 120, "hitl_gate": true}""",
                    currentStepIndex = 2,
                    totalSteps = 4,
                    estimatedCostUSD = 0.12,
                    actualCostUSD = 0.08,
                    tokensInput = 34200,
                    tokensOutput = 1250,
                    tokensReasoning = 8400,
                    logsText = """[10:48:02] INITIALIZING FinanceBot-v2 via Anthropic endpoint...
[10:48:04] Fetching input document: Q3_Financial_Report.pdf (2.4MB)
[10:48:08] Document parsed. Extracted revenue variance: +14.2% YoY.
[10:48:12] STEP 2/4 COMPLETE: Generated financial summary markdown.
[10:48:15] HITL GATE TRIGGERED: Agent requests write permission to update 14 records in [Users_DB].
[10:48:15] Awaiting human operator approval to execute database mutation...""",
                    hitlReason = "Agent requests write permission to update 14 records in [Users_DB]."
                ),
                TaskExecutionEntity(
                    id = "task_8403",
                    title = "ETL User Activity Logs to Data Warehouse",
                    agentId = "ag_data_extract",
                    agentName = "DataExtractionAgent",
                    flowType = "SEQUENTIAL",
                    status = "RUNNING",
                    objective = "Extract last 24h event streams from Kafka, transform into Parquet, load into BigQuery.",
                    parametersJson = """{"max_cost_cap": 2.00, "timeout": 180, "hitl_gate": false}""",
                    currentStepIndex = 3,
                    totalSteps = 5,
                    estimatedCostUSD = 0.45,
                    actualCostUSD = 0.28,
                    tokensInput = 112000,
                    tokensOutput = 4200,
                    tokensReasoning = 18000,
                    logsText = """[10:45:10] CONNECTED to Kafka brokers at kafka.internal:9092
[10:46:02] Consumed 84,200 events from topic user_activity_v1
[10:47:15] Transformation complete. Generating Parquet partition...
[10:48:00] Uploading partition chunk 3/5 to BigQuery table dw_staging.user_events...""",
                    hitlReason = ""
                ),
                TaskExecutionEntity(
                    id = "task_8399",
                    title = "Daily Security Scan & Patch Recommendation",
                    agentId = "ag_custom_rest",
                    agentName = "SecurityScannerAgent",
                    flowType = "PARALLEL_DAG",
                    status = "COMPLETED",
                    objective = "Run vulnerability scan across perimeter subnets and cross-reference CVE database.",
                    parametersJson = """{"max_cost_cap": 1.00, "timeout": 60, "hitl_gate": true}""",
                    currentStepIndex = 4,
                    totalSteps = 4,
                    estimatedCostUSD = 0.18,
                    actualCostUSD = 0.16,
                    tokensInput = 45000,
                    tokensOutput = 1800,
                    tokensReasoning = 6000,
                    logsText = """[08:00:00] TASK DISPATCHED: SecurityScannerAgent (DAG Parallel)
[08:00:15] Node A: Subnet ping complete (254 hosts active)
[08:00:30] Node B: CVE cross-reference match: 0 Critical, 2 Medium vulnerabilities found.
[08:01:00] Generated security report artifact: SEC-2026-0726.pdf
[08:01:05] TASK COMPLETED SUCCESSFULLY.""",
                    hitlReason = ""
                )
            )
            db.taskExecutionDao().insertTasks(initialTasks)
        }

        val currentMetrics = db.analyticsMetricDao().getAllMetrics().firstOrNull()
        if (currentMetrics.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val hourMs = 3600000L
            val sampleMetrics = mutableListOf<AnalyticsMetricEntity>()
            for (i in 0..12) {
                sampleMetrics.add(
                    AnalyticsMetricEntity(
                        timestamp = now - (i * hourMs * 2),
                        agentId = if (i % 2 == 0) "ag_finance_v2" else "ag_data_extract",
                        agentName = if (i % 2 == 0) "FinanceBot-v2" else "DataExtractionAgent",
                        tokensInput = kotlin.random.Random.nextLong(20000, 60000),
                        tokensOutput = kotlin.random.Random.nextLong(1000, 3000),
                        tokensReasoning = kotlin.random.Random.nextLong(4000, 12000),
                        costUSD = kotlin.random.Random.nextDouble(0.05, 0.35),
                        latencyMs = kotlin.random.Random.nextLong(1200, 4500),
                        isSuccess = i != 7
                    )
                )
            }
            db.analyticsMetricDao().insertMetrics(sampleMetrics)
        }

        val currentSettings = db.systemSettingsDao().getSettingsDirect()
        if (currentSettings == null) {
            db.systemSettingsDao().insertSettings(SystemSettingsEntity())
        }
    }

    suspend fun saveAgent(agent: AgentEntity) {
        db.agentDao().insertAgent(agent)
    }

    suspend fun deleteAgent(id: String) {
        db.agentDao().deleteAgent(id)
    }

    suspend fun saveTask(task: TaskExecutionEntity) {
        db.taskExecutionDao().insertTask(task)
    }

    suspend fun updateSettings(settings: SystemSettingsEntity) {
        db.systemSettingsDao().insertSettings(settings)
    }

    suspend fun approveHitlTask(taskId: String) {
        val task = db.taskExecutionDao().getTaskById(taskId) ?: return
        val nowLog = "\n[OPERATOR HITL APPROVED] Authorization granted by operator. Resuming execution..."
        val updatedTask = task.copy(
            status = "RUNNING",
            hitlReason = "",
            logsText = task.logsText + nowLog,
            updatedAt = System.currentTimeMillis()
        )
        db.taskExecutionDao().updateTask(updatedTask)
        db.agentDao().updateAgentStatus(task.agentId, "EXECUTING")

        // Simulate background completion
        scope.launch(Dispatchers.IO) {
            delay(2500)
            val finishLog = "\n[10:49:10] STEP 3/4 COMPLETE: Executed write query on [Users_DB] (14 rows modified)." +
                    "\n[10:49:15] STEP 4/4 COMPLETE: Generated execution audit record." +
                    "\n[10:49:16] TASK COMPLETED SUCCESSFULLY."
            val completedTask = updatedTask.copy(
                status = "COMPLETED",
                currentStepIndex = updatedTask.totalSteps,
                logsText = updatedTask.logsText + finishLog,
                updatedAt = System.currentTimeMillis()
            )
            db.taskExecutionDao().updateTask(completedTask)
            db.agentDao().updateAgentStatus(task.agentId, "ONLINE_IDLE")

            // Add metric
            db.analyticsMetricDao().insertMetric(
                AnalyticsMetricEntity(
                    timestamp = System.currentTimeMillis(),
                    agentId = task.agentId,
                    agentName = task.agentName,
                    tokensInput = task.tokensInput,
                    tokensOutput = task.tokensOutput,
                    tokensReasoning = task.tokensReasoning,
                    costUSD = task.actualCostUSD,
                    latencyMs = 3800,
                    isSuccess = true
                )
            )
        }
    }

    suspend fun rejectHitlTask(taskId: String) {
        val task = db.taskExecutionDao().getTaskById(taskId) ?: return
        val nowLog = "\n[OPERATOR HITL REJECTED] Operator denied permission. Task halted safely without mutations."
        val updatedTask = task.copy(
            status = "FAILED",
            hitlReason = "",
            logsText = task.logsText + nowLog,
            updatedAt = System.currentTimeMillis()
        )
        db.taskExecutionDao().updateTask(updatedTask)
        db.agentDao().updateAgentStatus(task.agentId, "ONLINE_IDLE")
    }

    suspend fun triggerEmergencyKillSwitch() {
        db.taskExecutionDao().killAllActiveTasks()
        db.agentDao().stopAllExecutingAgents()
        val currentSettings = db.systemSettingsDao().getSettingsDirect() ?: SystemSettingsEntity()
        db.systemSettingsDao().insertSettings(
            currentSettings.copy(emergencyKillSwitchEngaged = true)
        )
    }

    suspend fun resetKillSwitch() {
        val currentSettings = db.systemSettingsDao().getSettingsDirect() ?: SystemSettingsEntity()
        db.systemSettingsDao().insertSettings(
            currentSettings.copy(emergencyKillSwitchEngaged = false)
        )
    }

    suspend fun createAndRunTask(
        title: String,
        agentId: String,
        agentName: String,
        flowType: String,
        objective: String,
        maxCostCap: Double,
        timeoutSec: Int,
        hitlGate: Boolean
    ): String {
        val taskId = "task_" + kotlin.random.Random.nextInt(1000, 10000)
        val newTask = TaskExecutionEntity(
            id = taskId,
            title = title.ifBlank { "Task #${taskId.replace("task_", "")}" },
            agentId = agentId,
            agentName = agentName,
            flowType = flowType,
            status = if (hitlGate && objective.contains("database", ignoreCase = true)) "AWAITING_HITL" else "RUNNING",
            objective = objective,
            parametersJson = """{"max_cost_cap": $maxCostCap, "timeout": $timeoutSec, "hitl_gate": $hitlGate}""",
            currentStepIndex = 1,
            totalSteps = if (flowType == "PARALLEL_DAG") 5 else 3,
            estimatedCostUSD = 0.15,
            actualCostUSD = 0.03,
            tokensInput = 12500,
            tokensOutput = 480,
            tokensReasoning = 3200,
            logsText = """[${System.currentTimeMillis()}] INITIALIZING task execution on $agentName...
[AgentOS Proxy] Pre-flight check PASSED. Limits verified (Cap: $$maxCostCap, Timeout: ${timeoutSec}s).
[10:50:01] Processing goal prompt and loading agent tools...
[10:50:04] STEP 1/3 EXECUTING: Context resolution & vector recall...""",
            hitlReason = if (hitlGate && objective.contains("database", ignoreCase = true))
                "Agent requests write permission to update database records based on objective."
            else ""
        )
        db.taskExecutionDao().insertTask(newTask)
        db.agentDao().updateAgentStatus(agentId, if (newTask.status == "AWAITING_HITL") "AWAITING_APPROVAL" else "EXECUTING")

        if (newTask.status == "RUNNING") {
            scope.launch(Dispatchers.IO) {
                delay(3000)
                val taskAfter1 = db.taskExecutionDao().getTaskById(taskId) ?: return@launch
                if (taskAfter1.status == "KILLED") return@launch

                val updated2 = taskAfter1.copy(
                    currentStepIndex = 2,
                    logsText = taskAfter1.logsText + "\n[10:50:08] STEP 2/3 COMPLETE: Agent executed reasoning chain & generated response artifact."
                )
                db.taskExecutionDao().updateTask(updated2)

                delay(3000)
                val taskAfter2 = db.taskExecutionDao().getTaskById(taskId) ?: return@launch
                if (taskAfter2.status == "KILLED") return@launch

                val completed = taskAfter2.copy(
                    status = "COMPLETED",
                    currentStepIndex = taskAfter2.totalSteps,
                    actualCostUSD = 0.12,
                    logsText = taskAfter2.logsText + "\n[10:50:12] STEP 3/3 COMPLETE: Validation verified.\n[10:50:13] TASK COMPLETED SUCCESSFULLY.",
                    updatedAt = System.currentTimeMillis()
                )
                db.taskExecutionDao().updateTask(completed)
                db.agentDao().updateAgentStatus(agentId, "ONLINE_IDLE")

                db.analyticsMetricDao().insertMetric(
                    AnalyticsMetricEntity(
                        timestamp = System.currentTimeMillis(),
                        agentId = agentId,
                        agentName = agentName,
                        tokensInput = 12500,
                        tokensOutput = 950,
                        tokensReasoning = 3200,
                        costUSD = 0.12,
                        latencyMs = 6200,
                        isSuccess = true
                    )
                )
            }
        }

        return taskId
    }
}
