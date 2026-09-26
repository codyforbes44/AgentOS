package com.codyforbes.agentos.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.codyforbes.agentos.data.TaskExecutionEntity
import com.codyforbes.agentos.ui.components.HitlDecisionSheet
import com.codyforbes.agentos.ui.theme.AgentOSTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HitlDecisionUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun approveAndRejectInvokeTheOperatorActions() {
        val task = TaskExecutionEntity(
            id = "task_8402",
            title = "Audit Q3",
            agentId = "ag_finance_v2",
            agentName = "FinanceBot-v2",
            flowType = "SINGLE",
            status = "AWAITING_HITL",
            objective = "Update records",
            parametersJson = "{}",
            currentStepIndex = 2,
            totalSteps = 4,
            estimatedCostUSD = 0.12,
            actualCostUSD = 0.08,
            tokensInput = 10,
            tokensOutput = 2,
            tokensReasoning = 1,
            logsText = "waiting",
            hitlReason = "Agent requests write permission to update 14 records.",
        )
        var approved = 0
        var rejected = 0
        composeRule.setContent {
            AgentOSTheme {
                HitlDecisionSheet(
                    task = task,
                    onApprove = { approved += 1 },
                    onReject = { rejected += 1 },
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithTag("reject_hitl_action").performClick()
        composeRule.onNodeWithTag("approve_hitl_action").performClick()

        assertEquals(1, rejected)
        assertEquals(1, approved)
    }
}
