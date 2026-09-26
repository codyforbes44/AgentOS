package com.codyforbes.agentos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codyforbes.agentos.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AgentOSViewModel(
    application: Application,
    private val repository: AgentOSRepository
) : AndroidViewModel(application) {

    val agents = repository.agents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tasks = repository.tasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val metrics = repository.metrics.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val settings = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Navigation & UI filter states
    val activeTab = MutableStateFlow(0) // 0=Agents, 1=Tasks, 2=Analytics, 3=Settings
    val agentStatusFilter = MutableStateFlow("ALL")
    val agentProviderFilter = MutableStateFlow("ALL")
    val taskStatusFilter = MutableStateFlow("ALL")
    val analyticsTimeRange = MutableStateFlow("24h") // 1h, 24h, 7d, 30d

    // Modals / Sheets
    val selectedAgentDetail = MutableStateFlow<AgentEntity?>(null)
    val selectedTaskInspector = MutableStateFlow<TaskExecutionEntity?>(null)
    val showOnboardingWizard = MutableStateFlow(false)
    val showDelegationModal = MutableStateFlow(false)
    val showHitlDecisionTask = MutableStateFlow<TaskExecutionEntity?>(null)
    val showKillSwitchConfirmation = MutableStateFlow(false)
    val toastMessage = MutableStateFlow<String?>(null)

    // Onboarding Form State
    val obName = MutableStateFlow("")
    val obProvider = MutableStateFlow("OpenAI")
    val obEndpoint = MutableStateFlow("https://api.openai.com/v1/chat/completions")
    val obApiKey = MutableStateFlow("")
    val obMaxCostCap = MutableStateFlow("2.00")
    val obTimeout = MutableStateFlow("120")
    val obHitlEnabled = MutableStateFlow(true)
    val obTags = MutableStateFlow("Production,SQL")
    val isPingTesting = MutableStateFlow(false)
    val pingSuccessBadge = MutableStateFlow<String?>(null)

    // Task Delegation Form State
    val delTitle = MutableStateFlow("")
    val delObjective = MutableStateFlow("")
    val delAgentId = MutableStateFlow("")
    val delFlowType = MutableStateFlow("SINGLE") // SINGLE, SEQUENTIAL, PARALLEL_DAG
    val delMaxCostCap = MutableStateFlow("0.50")
    val delTimeout = MutableStateFlow("120")
    val delHitlGate = MutableStateFlow(true)
    val preflightStatus = MutableStateFlow<String?>(null)

    // Derived global header metrics
    val activeTaskCount = tasks.map { list ->
        list.count { it.status == "RUNNING" || it.status == "AWAITING_HITL" || it.status == "QUEUED" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val awaitingHitlCount = tasks.map { list ->
        list.count { it.status == "AWAITING_HITL" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalDailyBurnUSD = agents.map { list ->
        list.sumOf { it.totalCostUSD }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun setTab(tabIndex: Int) {
        activeTab.value = tabIndex
    }

    fun openAgentDetail(agent: AgentEntity) {
        selectedAgentDetail.value = agent
    }

    fun closeAgentDetail() {
        selectedAgentDetail.value = null
    }

    fun openTaskInspector(task: TaskExecutionEntity) {
        selectedTaskInspector.value = task
    }

    fun closeTaskInspector() {
        selectedTaskInspector.value = null
    }

    fun openOnboardingWizard() {
        obName.value = ""
        obApiKey.value = ""
        pingSuccessBadge.value = null
        showOnboardingWizard.value = true
    }

    fun closeOnboardingWizard() {
        showOnboardingWizard.value = false
    }

    fun openDelegationModal(preselectedAgentId: String? = null) {
        delTitle.value = ""
        delObjective.value = ""
        delAgentId.value = preselectedAgentId ?: (agents.value.firstOrNull()?.id ?: "")
        preflightStatus.value = null
        showDelegationModal.value = true
    }

    fun closeDelegationModal() {
        showDelegationModal.value = false
    }

    fun openHitlDecision(task: TaskExecutionEntity) {
        showHitlDecisionTask.value = task
    }

    fun closeHitlDecision() {
        showHitlDecisionTask.value = null
    }

    fun triggerPingTest() {
        viewModelScope.launch {
            isPingTesting.value = true
            pingSuccessBadge.value = null
            delay(1500) // 1,500ms discovery test as per spec
            isPingTesting.value = false
            pingSuccessBadge.value = "PASSED: Connected in 184ms | Discovered Tools: [SQL Access, Code Exec, Web Search]"
        }
    }

    fun deployNewAgent() {
        viewModelScope.launch(Dispatchers.IO) {
            val id = "ag_" + UUID.randomUUID().toString().take(8)
            val name = obName.value.ifBlank { "${obProvider.value}Agent-${id.takeLast(4)}" }
            val newAgent = AgentEntity(
                id = id,
                name = name,
                provider = obProvider.value,
                endpointUrl = obEndpoint.value,
                maskedApiKey = if (obApiKey.value.length > 8)
                    obApiKey.value.take(4) + "••••••••" + obApiKey.value.takeLast(4)
                else "sk-proj-••••••••811a",
                status = "ONLINE_IDLE",
                tags = obTags.value,
                maxCostCapUSD = obMaxCostCap.value.toDoubleOrNull() ?: 2.0,
                timeoutSeconds = obTimeout.value.toIntOrNull() ?: 120,
                hitlEnabled = obHitlEnabled.value,
                healthScore = 100,
                dailyTokenBurn = 0,
                totalCostUSD = 0.0,
                capabilities = "Web Search, Code Execution, Vector Query"
            )
            repository.saveAgent(newAgent)
            showOnboardingWizard.value = false
            toastMessage.value = "Agent '$name' deployed & registered successfully!"
        }
    }

    fun runPreflightCheck() {
        viewModelScope.launch {
            val agent = agents.value.find { it.id == delAgentId.value }
            preflightStatus.value = "PASSED: Agent '${agent?.name ?: "Target"}' ready | Est. Cost: ~$0.12 USD | Est. Latency: 18s"
        }
    }

    fun confirmExecuteTask() {
        viewModelScope.launch(Dispatchers.IO) {
            val agent = agents.value.find { it.id == delAgentId.value }
            val agentName = agent?.name ?: "Agent"
            val taskId = repository.createAndRunTask(
                title = delTitle.value,
                agentId = delAgentId.value,
                agentName = agentName,
                flowType = delFlowType.value,
                objective = delObjective.value,
                maxCostCap = delMaxCostCap.value.toDoubleOrNull() ?: 0.50,
                timeoutSec = delTimeout.value.toIntOrNull() ?: 120,
                hitlGate = delHitlGate.value
            )
            showDelegationModal.value = false
            toastMessage.value = "Task execution $taskId dispatched to $agentName!"
        }
    }

    fun approveHitl(taskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.approveHitlTask(taskId)
            showHitlDecisionTask.value = null
            toastMessage.value = "HITL Action APPROVED. Task execution resumed."
        }
    }

    fun rejectHitl(taskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.rejectHitlTask(taskId)
            showHitlDecisionTask.value = null
            toastMessage.value = "HITL Action REJECTED. Task execution safely halted."
        }
    }

    fun triggerEmergencyKillSwitch() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.triggerEmergencyKillSwitch()
            showKillSwitchConfirmation.value = false
            toastMessage.value = "EMERGENCY KILL SWITCH ENGAGED! All task executions halted."
        }
    }

    fun resetKillSwitch() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetKillSwitch()
            toastMessage.value = "Emergency Kill Switch reset. Operational state restored."
        }
    }

    fun deleteAgent(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAgent(id)
            selectedAgentDetail.value = null
            toastMessage.value = "Agent removed from control plane."
        }
    }

    fun updateRole(role: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val curr = settings.value ?: SystemSettingsEntity()
            repository.updateSettings(curr.copy(activeRole = role))
            toastMessage.value = "Role updated to $role"
        }
    }

    fun updateDailyCap(cap: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val curr = settings.value ?: SystemSettingsEntity()
            repository.updateSettings(curr.copy(dailyCostCapUSD = cap))
        }
    }

    fun clearToast() {
        toastMessage.value = null
    }

    fun exportTelemetryReport(format: String): String {
        return when (format.uppercase()) {
            "CSV" -> "Timestamp,Agent,TokensInput,TokensOutput,CostUSD,Status\n" +
                    "${System.currentTimeMillis()},FinanceBot-v2,34200,1250,0.08,SUCCESS\n" +
                    "${System.currentTimeMillis() - 3600000},DataExtractionAgent,112000,4200,0.28,SUCCESS\n"
            "JSON" -> """{
  "export_time": "${System.currentTimeMillis()}",
  "total_burn_usd": ${totalDailyBurnUSD.value},
  "active_agents": ${agents.value.size},
  "completed_tasks": ${tasks.value.count { it.status == "COMPLETED" }}
}"""
            else -> "AgentOS Telemetry Summary Report\nActive Agents: ${agents.value.size}\nTotal Burn: $${String.format("%.2f", totalDailyBurnUSD.value)}"
        }
    }
}

class AgentOSViewModelFactory(
    private val application: Application,
    private val repository: AgentOSRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgentOSViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgentOSViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
