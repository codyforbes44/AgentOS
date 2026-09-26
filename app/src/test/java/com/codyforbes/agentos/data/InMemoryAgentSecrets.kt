package com.codyforbes.agentos.data

class InMemoryAgentSecrets : AgentSecrets {
    private val values = mutableMapOf<String, String>()

    override suspend fun put(agentId: String, apiKey: String) {
        values[agentId] = apiKey
    }

    override suspend fun delete(agentId: String) {
        values.remove(agentId)
    }

    override suspend fun get(agentId: String): String? = values[agentId]
}
