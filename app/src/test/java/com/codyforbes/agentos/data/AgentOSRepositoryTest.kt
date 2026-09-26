package com.codyforbes.agentos.data

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AgentOSRepositoryTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var db: AgentOSDatabase
    private lateinit var secrets: InMemoryAgentSecrets
    private lateinit var repository: AgentOSRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(context, AgentOSDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
        secrets = InMemoryAgentSecrets()
        repository = AgentOSRepository(
            db = db,
            secrets = secrets,
            backgroundScope = CoroutineScope(SupervisorJob() + dispatcher),
            seedSampleData = false,
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun apiKeyIsStoredOutsideRoomAndOnlyAHintIsPersisted() = runTest(dispatcher) {
        val rawKey = "sk-live-should-not-be-stored-91ab"
        repository.saveAgent(sampleAgent(maskedApiKey = displayHintForApiKey(rawKey)))
        repository.storeAgentSecret("ag_1", rawKey)

        val stored = db.agentDao().getAgentById("ag_1")
        assertEquals("••••91ab", stored?.maskedApiKey)
        assertFalse(stored?.maskedApiKey?.contains("sk-live") == true)
        assertEquals(rawKey, secrets.get("ag_1"))

        repository.deleteAgent("ag_1")
        assertNull(db.agentDao().getAgentById("ag_1"))
        assertNull(secrets.get("ag_1"))
    }

    @Test
    fun approveMovesAwaitingTaskToRunning() = runTest(dispatcher) {
        repository.saveAgent(sampleAgent())
        repository.saveTask(sampleTask(status = "AWAITING_HITL"))

        repository.approveHitlTask("task_1")

        assertEquals("RUNNING", db.taskExecutionDao().getTaskById("task_1")?.status)
        assertEquals("EXECUTING", db.agentDao().getAgentById("ag_1")?.status)
    }

    @Test
    fun rejectMovesAwaitingTaskToFailed() = runTest(dispatcher) {
        repository.saveAgent(sampleAgent())
        repository.saveTask(sampleTask(status = "AWAITING_HITL"))

        repository.rejectHitlTask("task_1")

        assertEquals("FAILED", db.taskExecutionDao().getTaskById("task_1")?.status)
        assertEquals("ONLINE_IDLE", db.agentDao().getAgentById("ag_1")?.status)
    }

    @Test
    fun killSwitchMarksActiveWorkAndStaysEngaged() = runTest(dispatcher) {
        repository.saveAgent(sampleAgent(status = "EXECUTING"))
        repository.saveTask(sampleTask(status = "RUNNING"))

        repository.triggerEmergencyKillSwitch()

        assertEquals("KILLED", db.taskExecutionDao().getTaskById("task_1")?.status)
        assertEquals("OFFLINE", db.agentDao().getAgentById("ag_1")?.status)
        assertTrue(db.systemSettingsDao().getSettingsDirect()?.emergencyKillSwitchEngaged == true)
    }

    @Test
    fun inFlightApprovalCannotCompleteAKilledTask() = runTest(dispatcher) {
        repository.saveAgent(sampleAgent())
        repository.saveTask(sampleTask(status = "AWAITING_HITL"))

        repository.approveHitlTask("task_1")
        repository.triggerEmergencyKillSwitch()
        advanceTimeBy(3_000)
        advanceUntilIdle()

        assertEquals("KILLED", db.taskExecutionDao().getTaskById("task_1")?.status)
        assertTrue(db.systemSettingsDao().getSettingsDirect()?.emergencyKillSwitchEngaged == true)
    }
}

private fun sampleAgent(
    status: String = "AWAITING_APPROVAL",
    maskedApiKey: String = "••••91ab",
) = AgentEntity(
    id = "ag_1",
    name = "FinanceBot-v2",
    provider = "Anthropic",
    endpointUrl = "https://api.anthropic.com/v1/messages",
    maskedApiKey = maskedApiKey,
    status = status,
    tags = "Finance",
    maxCostCapUSD = 1.5,
    timeoutSeconds = 120,
    hitlEnabled = true,
    healthScore = 98,
    dailyTokenBurn = 0,
    totalCostUSD = 0.0,
    capabilities = "SQL",
)

private fun sampleTask(status: String) = TaskExecutionEntity(
    id = "task_1",
    title = "Audit",
    agentId = "ag_1",
    agentName = "FinanceBot-v2",
    flowType = "SINGLE",
    status = status,
    objective = "Update records",
    parametersJson = "{}",
    currentStepIndex = 2,
    totalSteps = 4,
    estimatedCostUSD = 0.12,
    actualCostUSD = 0.08,
    tokensInput = 10,
    tokensOutput = 2,
    tokensReasoning = 1,
    logsText = "start",
    hitlReason = "Write requested",
)
