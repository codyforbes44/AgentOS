package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AgentEntity
import com.example.data.AgentOSRepository
import com.example.data.TaskExecutionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AgentViewModel(
    private val repository: AgentOSRepository
) : ViewModel() {

    // Connected agents list observed directly from repository
    val agents: StateFlow<List<AgentEntity>> = repository.agents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Task list observed directly from repository
    val tasks: StateFlow<List<TaskExecutionEntity>> = repository.tasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current active task count managed using Kotlin StateFlow
    val currentTaskCount: StateFlow<Int> = tasks.map { list ->
        list.count { it.status == "RUNNING" || it.status == "AWAITING_HITL" || it.status == "EXECUTING" || it.status == "QUEUED" }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Total task count managed using Kotlin StateFlow
    val totalTaskCount: StateFlow<Int> = tasks.map { it.size }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Filter state for agents (ALL, ONLINE_IDLE, EXECUTING, AWAITING_APPROVAL, etc.)
    val agentStatusFilter = MutableStateFlow("ALL")

    // Filtered agent list ready to be observed by UI
    val filteredAgents: StateFlow<List<AgentEntity>> = combine(agents, agentStatusFilter) { list, filter ->
        if (filter == "ALL") {
            list
        } else {
            list.filter { it.status == filter }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setStatusFilter(filter: String) {
        agentStatusFilter.value = filter
    }

    fun registerAgent(
        name: String,
        provider: String,
        endpointUrl: String,
        apiKey: String,
        maxCostCapUSD: Double,
        timeoutSec: Int,
        hitlGate: Boolean,
        tags: String
    ) {
        viewModelScope.launch {
            val id = "ag_" + java.util.UUID.randomUUID().toString().take(8)
            val maskedKey = if (apiKey.length > 8) "sk-..." + apiKey.takeLast(4) else "sk-****"
            val agent = AgentEntity(
                id = id,
                name = name.ifBlank { "Custom-Agent" },
                provider = provider,
                endpointUrl = endpointUrl,
                maskedApiKey = maskedKey,
                status = "ONLINE_IDLE",
                tags = tags.ifBlank { "General,REST" },
                maxCostCapUSD = maxCostCapUSD,
                timeoutSeconds = timeoutSec,
                hitlEnabled = hitlGate,
                healthScore = 100,
                dailyTokenBurn = 0L,
                totalCostUSD = 0.0,
                capabilities = "REST API, Custom Pipeline"
            )
            repository.saveAgent(agent)
        }
    }

    fun deleteAgent(agentId: String) {
        viewModelScope.launch {
            repository.deleteAgent(agentId)
        }
    }
}

class AgentViewModelFactory(
    private val repository: AgentOSRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgentViewModel::class.java)) {
            return AgentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
