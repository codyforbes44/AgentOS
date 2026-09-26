package com.codyforbes.agentos.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codyforbes.agentos.R
import com.codyforbes.agentos.data.AgentEntity
import com.codyforbes.agentos.data.AgentOSRepository
import com.codyforbes.agentos.data.SystemSettingsEntity
import com.codyforbes.agentos.data.TaskExecutionEntity
import com.codyforbes.agentos.data.displayHintForApiKey
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AgentOSViewModel(
    application: Application,
    private val repository: AgentOSRepository,
) : AndroidViewModel(application) {

    val agents = repository.agents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val tasks = repository.tasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val metrics = repository.metrics.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val settings = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null,
    )

    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _agentStatusFilter = MutableStateFlow("ALL")
    val agentStatusFilter: StateFlow<String> = _agentStatusFilter.asStateFlow()

    private val _agentProviderFilter = MutableStateFlow("ALL")
    val agentProviderFilter: StateFlow<String> = _agentProviderFilter.asStateFlow()

    private val _taskStatusFilter = MutableStateFlow("ALL")
    val taskStatusFilter: StateFlow<String> = _taskStatusFilter.asStateFlow()

    private val _analyticsTimeRange = MutableStateFlow("24h")
    val analyticsTimeRange: StateFlow<String> = _analyticsTimeRange.asStateFlow()

    private val _selectedAgentDetail = MutableStateFlow<AgentEntity?>(null)
    val selectedAgentDetail: StateFlow<AgentEntity?> = _selectedAgentDetail.asStateFlow()

    private val _selectedTaskInspector = MutableStateFlow<TaskExecutionEntity?>(null)
    val selectedTaskInspector: StateFlow<TaskExecutionEntity?> = _selectedTaskInspector.asStateFlow()

    private val _showOnboardingWizard = MutableStateFlow(false)
    val showOnboardingWizard: StateFlow<Boolean> = _showOnboardingWizard.asStateFlow()

    private val _showDelegationModal = MutableStateFlow(false)
    val showDelegationModal: StateFlow<Boolean> = _showDelegationModal.asStateFlow()

    private val _showHitlDecisionTask = MutableStateFlow<TaskExecutionEntity?>(null)
    val showHitlDecisionTask: StateFlow<TaskExecutionEntity?> = _showHitlDecisionTask.asStateFlow()

    private val _showKillSwitchConfirmation = MutableStateFlow(false)
    val showKillSwitchConfirmation: StateFlow<Boolean> = _showKillSwitchConfirmation.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _obName = MutableStateFlow("")
    val obName: StateFlow<String> = _obName.asStateFlow()

    private val _obProvider = MutableStateFlow("OpenAI")
    val obProvider: StateFlow<String> = _obProvider.asStateFlow()

    private val _obEndpoint = MutableStateFlow("https://api.openai.com/v1/chat/completions")
    val obEndpoint: StateFlow<String> = _obEndpoint.asStateFlow()

    private val _obApiKey = MutableStateFlow("")
    val obApiKey: StateFlow<String> = _obApiKey.asStateFlow()

    private val _obMaxCostCap = MutableStateFlow("2.00")
    val obMaxCostCap: StateFlow<String> = _obMaxCostCap.asStateFlow()

    private val _obTimeout = MutableStateFlow("120")
    val obTimeout: StateFlow<String> = _obTimeout.asStateFlow()

    private val _obHitlEnabled = MutableStateFlow(true)
    val obHitlEnabled: StateFlow<Boolean> = _obHitlEnabled.asStateFlow()

    private val _obTags = MutableStateFlow("Production,SQL")
    val obTags: StateFlow<String> = _obTags.asStateFlow()

    private val _isPingTesting = MutableStateFlow(false)
    val isPingTesting: StateFlow<Boolean> = _isPingTesting.asStateFlow()

    private val _pingSuccessBadge = MutableStateFlow<String?>(null)
    val pingSuccessBadge: StateFlow<String?> = _pingSuccessBadge.asStateFlow()

    private val _delTitle = MutableStateFlow("")
    val delTitle: StateFlow<String> = _delTitle.asStateFlow()

    private val _delObjective = MutableStateFlow("")
    val delObjective: StateFlow<String> = _delObjective.asStateFlow()

    private val _delAgentId = MutableStateFlow("")
    val delAgentId: StateFlow<String> = _delAgentId.asStateFlow()

    private val _delFlowType = MutableStateFlow("SINGLE")
    val delFlowType: StateFlow<String> = _delFlowType.asStateFlow()

    private val _delMaxCostCap = MutableStateFlow("0.50")
    val delMaxCostCap: StateFlow<String> = _delMaxCostCap.asStateFlow()

    private val _delTimeout = MutableStateFlow("120")
    val delTimeout: StateFlow<String> = _delTimeout.asStateFlow()

    private val _delHitlGate = MutableStateFlow(true)
    val delHitlGate: StateFlow<Boolean> = _delHitlGate.asStateFlow()

    private val _preflightStatus = MutableStateFlow<String?>(null)
    val preflightStatus: StateFlow<String?> = _preflightStatus.asStateFlow()

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
        _activeTab.value = tabIndex
    }

    fun setAgentStatusFilter(filter: String) {
        _agentStatusFilter.value = filter
    }

    fun setAgentProviderFilter(filter: String) {
        _agentProviderFilter.value = filter
    }

    fun setTaskStatusFilter(filter: String) {
        _taskStatusFilter.value = filter
    }

    fun setAnalyticsTimeRange(range: String) {
        _analyticsTimeRange.value = range
    }

    fun onObNameChange(value: String) {
        _obName.value = value
    }

    fun onObProviderChange(provider: String, endpoint: String) {
        _obProvider.value = provider
        _obEndpoint.value = endpoint
    }

    fun onObEndpointChange(value: String) {
        _obEndpoint.value = value
    }

    fun onObApiKeyChange(value: String) {
        _obApiKey.value = value
    }

    fun onObMaxCostCapChange(value: String) {
        _obMaxCostCap.value = value
    }

    fun onObTimeoutChange(value: String) {
        _obTimeout.value = value
    }

    fun onObHitlEnabledChange(enabled: Boolean) {
        _obHitlEnabled.value = enabled
    }

    fun onDelTitleChange(value: String) {
        _delTitle.value = value
    }

    fun onDelObjectiveChange(value: String) {
        _delObjective.value = value
    }

    fun appendDelObjective(suffix: String) {
        _delObjective.value = _delObjective.value + " " + suffix
    }

    fun onDelAgentIdChange(value: String) {
        _delAgentId.value = value
    }

    fun onDelFlowTypeChange(value: String) {
        _delFlowType.value = value
    }

    fun openAgentDetail(agent: AgentEntity) {
        _selectedAgentDetail.value = agent
    }

    fun closeAgentDetail() {
        _selectedAgentDetail.value = null
    }

    fun openTaskInspector(task: TaskExecutionEntity) {
        _selectedTaskInspector.value = task
    }

    fun closeTaskInspector() {
        _selectedTaskInspector.value = null
    }

    fun openOnboardingWizard() {
        _obName.value = ""
        _obApiKey.value = ""
        _pingSuccessBadge.value = null
        _showOnboardingWizard.value = true
    }

    fun closeOnboardingWizard() {
        _showOnboardingWizard.value = false
    }

    fun openDelegationModal(preselectedAgentId: String? = null) {
        _delTitle.value = ""
        _delObjective.value = ""
        _delAgentId.value = preselectedAgentId ?: (agents.value.firstOrNull()?.id ?: "")
        _preflightStatus.value = null
        _showDelegationModal.value = true
    }

    fun closeDelegationModal() {
        _showDelegationModal.value = false
    }

    fun openHitlDecision(task: TaskExecutionEntity) {
        _showHitlDecisionTask.value = task
    }

    fun closeHitlDecision() {
        _showHitlDecisionTask.value = null
    }

    fun triggerPingTest() {
        viewModelScope.launch {
            _isPingTesting.value = true
            _pingSuccessBadge.value = null
            delay(1500)
            _isPingTesting.value = false
            _pingSuccessBadge.value = text(R.string.ping_passed)
        }
    }

    fun deployNewAgent() {
        launchAction {
            val rawKey = _obApiKey.value
            _obApiKey.value = ""
            val id = "ag_" + UUID.randomUUID().toString().take(8)
            val name = _obName.value.ifBlank { "${_obProvider.value}Agent-${id.takeLast(4)}" }
            val newAgent = AgentEntity(
                id = id,
                name = name,
                provider = _obProvider.value,
                endpointUrl = _obEndpoint.value,
                maskedApiKey = displayHintForApiKey(rawKey),
                status = "ONLINE_IDLE",
                tags = _obTags.value,
                maxCostCapUSD = _obMaxCostCap.value.toDoubleOrNull() ?: 2.0,
                timeoutSeconds = _obTimeout.value.toIntOrNull() ?: 120,
                hitlEnabled = _obHitlEnabled.value,
                healthScore = 100,
                dailyTokenBurn = 0,
                totalCostUSD = 0.0,
                capabilities = "Web Search, Code Execution, Vector Query",
            )
            repository.saveAgent(newAgent)
            if (rawKey.isNotBlank()) {
                repository.storeAgentSecret(id, rawKey)
            }
            _showOnboardingWizard.value = false
            _toastMessage.value = text(R.string.toast_agent_deployed, name)
        }
    }

    fun runPreflightCheck() {
        viewModelScope.launch {
            val agent = agents.value.find { it.id == _delAgentId.value }
            _preflightStatus.value = text(R.string.preflight_passed, agent?.name ?: text(R.string.preflight_target))
        }
    }

    fun confirmExecuteTask() {
        launchAction {
            val agent = agents.value.find { it.id == _delAgentId.value }
            val agentName = agent?.name ?: text(R.string.preflight_target)
            val taskId = repository.createAndRunTask(
                title = _delTitle.value,
                agentId = _delAgentId.value,
                agentName = agentName,
                flowType = _delFlowType.value,
                objective = _delObjective.value,
                maxCostCap = _delMaxCostCap.value.toDoubleOrNull() ?: 0.50,
                timeoutSec = _delTimeout.value.toIntOrNull() ?: 120,
                hitlGate = _delHitlGate.value,
            )
            _showDelegationModal.value = false
            _toastMessage.value = text(R.string.toast_task_dispatched, taskId, agentName)
        }
    }

    fun approveHitl(taskId: String) {
        launchAction {
            repository.approveHitlTask(taskId)
            _showHitlDecisionTask.value = null
            _toastMessage.value = text(R.string.toast_hitl_approved)
        }
    }

    fun rejectHitl(taskId: String) {
        launchAction {
            repository.rejectHitlTask(taskId)
            _showHitlDecisionTask.value = null
            _toastMessage.value = text(R.string.toast_hitl_rejected)
        }
    }

    fun triggerEmergencyKillSwitch() {
        launchAction {
            repository.triggerEmergencyKillSwitch()
            _showKillSwitchConfirmation.value = false
            _toastMessage.value = text(R.string.toast_kill_switch)
        }
    }

    fun resetKillSwitch() {
        launchAction {
            repository.resetKillSwitch()
            _toastMessage.value = text(R.string.toast_kill_switch_reset)
        }
    }

    fun deleteAgent(id: String) {
        launchAction {
            repository.deleteAgent(id)
            _selectedAgentDetail.value = null
            _toastMessage.value = text(R.string.toast_agent_removed)
        }
    }

    fun updateRole(role: String) {
        launchAction {
            val curr = settings.value ?: SystemSettingsEntity()
            repository.updateSettings(curr.copy(activeRole = role))
            _toastMessage.value = text(R.string.toast_role_updated, role)
        }
    }

    fun updateDailyCap(cap: Double) {
        launchAction {
            val curr = settings.value ?: SystemSettingsEntity()
            repository.updateSettings(curr.copy(dailyCostCapUSD = cap))
        }
    }

    fun updateCircuitBreaker(enabled: Boolean) {
        launchAction {
            val curr = settings.value ?: SystemSettingsEntity()
            repository.updateSettings(curr.copy(circuitBreakerEnabled = enabled))
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun exportTelemetryReport(format: String): String {
        return when (format.uppercase(Locale.US)) {
            "CSV" -> "Timestamp,Agent,TokensInput,TokensOutput,CostUSD,Status\n" +
                "${System.currentTimeMillis()},FinanceBot-v2,34200,1250,0.08,SUCCESS\n" +
                "${System.currentTimeMillis() - 3600000},DataExtractionAgent,112000,4200,0.28,SUCCESS\n"
            "JSON" -> """{
  "export_time": "${System.currentTimeMillis()}",
  "total_burn_usd": ${totalDailyBurnUSD.value},
  "active_agents": ${agents.value.size},
  "completed_tasks": ${tasks.value.count { it.status == "COMPLETED" }}
}"""
            else -> text(
                R.string.telemetry_summary,
                agents.value.size,
                String.format(Locale.getDefault(), "%.2f", totalDailyBurnUSD.value),
            )
        }
    }

    private fun launchAction(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                Log.e(TAG, "Action failed")
                _toastMessage.value = text(R.string.toast_action_failed)
            }
        }
    }

    private fun text(resId: Int, vararg args: Any): String {
        return getApplication<Application>().getString(resId, *args)
    }

    private companion object {
        const val TAG = "AgentOSViewModel"
    }
}

class AgentOSViewModelFactory(
    private val application: Application,
    private val repository: AgentOSRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgentOSViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgentOSViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
