package com.codyforbes.agentos.viewmodel

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.codyforbes.agentos.data.AgentEntity
import com.codyforbes.agentos.data.AgentOSDatabase
import com.codyforbes.agentos.data.AgentOSRepository
import com.codyforbes.agentos.data.InMemoryAgentSecrets
import com.codyforbes.agentos.data.TaskExecutionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AgentOSViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var db: AgentOSDatabase
    private lateinit var viewModel: AgentOSViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val context = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(context, AgentOSDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
        val repository = AgentOSRepository(
            db = db,
            secrets = InMemoryAgentSecrets(),
            backgroundScope = CoroutineScope(SupervisorJob() + dispatcher),
            seedSampleData = false,
        )
        viewModel = AgentOSViewModel(context, repository)
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun approvingATaskClosesTheDecisionSheet() = runTest(dispatcher) {
        val task = TaskExecutionEntity(
            id = "task_1",
            title = "Audit",
            agentId = "ag_1",
            agentName = "FinanceBot-v2",
            flowType = "SINGLE",
            status = "AWAITING_HITL",
            objective = "Update records",
            parametersJson = "{}",
            currentStepIndex = 1,
            totalSteps = 3,
            estimatedCostUSD = 0.1,
            actualCostUSD = 0.05,
            tokensInput = 1,
            tokensOutput = 1,
            tokensReasoning = 1,
            logsText = "start",
            hitlReason = "Write requested",
        )
        db.agentDao().insertAgent(
            AgentEntity(
                id = "ag_1",
                name = "FinanceBot-v2",
                provider = "Anthropic",
                endpointUrl = "https://api.anthropic.com/v1/messages",
                maskedApiKey = "••••91ab",
                status = "AWAITING_APPROVAL",
                tags = "Finance",
                maxCostCapUSD = 1.0,
                timeoutSeconds = 60,
                hitlEnabled = true,
                healthScore = 90,
                dailyTokenBurn = 0,
                totalCostUSD = 0.0,
                capabilities = "SQL",
            ),
        )
        db.taskExecutionDao().insertTask(task)
        viewModel.openHitlDecision(task)

        viewModel.approveHitl(task.id)
        advanceUntilIdle()

        assertNull(viewModel.showHitlDecisionTask.value)
    }
}
