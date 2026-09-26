package com.codyforbes.agentos.data

/**
 * Stores agent API keys outside the Room database.
 * Room keeps only a non-reversible display hint.
 */
interface AgentSecrets {
    suspend fun put(agentId: String, apiKey: String)

    suspend fun delete(agentId: String)

    suspend fun get(agentId: String): String?
}

/** Last four characters only. Never includes a key prefix. */
fun displayHintForApiKey(apiKey: String): String {
    val trimmed = apiKey.trim()
    if (trimmed.length < 4) return "••••"
    return "••••" + trimmed.takeLast(4)
}
