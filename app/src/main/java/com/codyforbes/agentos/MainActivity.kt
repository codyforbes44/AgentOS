package com.codyforbes.agentos

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codyforbes.agentos.data.AgentOSDatabase
import com.codyforbes.agentos.ui.components.AgentOSBottomNavigation
import com.codyforbes.agentos.ui.components.GlobalHeader
import com.codyforbes.agentos.ui.components.HitlDecisionSheet
import com.codyforbes.agentos.ui.screens.*
import com.codyforbes.agentos.ui.theme.AgentOSTheme
import com.codyforbes.agentos.viewmodel.AgentOSViewModel
import com.codyforbes.agentos.viewmodel.AgentOSViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: AgentOSViewModel by viewModels {
        val repository = AgentOSDatabase.getRepository(applicationContext)
        AgentOSViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AgentOSTheme {
                val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
                val activeTasks by viewModel.activeTaskCount.collectAsStateWithLifecycle()
                val hitlCount by viewModel.awaitingHitlCount.collectAsStateWithLifecycle()
                val dailyBurn by viewModel.totalDailyBurnUSD.collectAsStateWithLifecycle()
                val settings by viewModel.settings.collectAsStateWithLifecycle()
                val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()

                val agents by viewModel.agents.collectAsStateWithLifecycle()
                val selectedAgentDetail by viewModel.selectedAgentDetail.collectAsStateWithLifecycle()
                val selectedTaskInspector by viewModel.selectedTaskInspector.collectAsStateWithLifecycle()
                val showOnboardingWizard by viewModel.showOnboardingWizard.collectAsStateWithLifecycle()
                val showDelegationModal by viewModel.showDelegationModal.collectAsStateWithLifecycle()
                val showHitlTask by viewModel.showHitlDecisionTask.collectAsStateWithLifecycle()

                val isKillSwitchEngaged = settings?.emergencyKillSwitchEngaged == true
                val modalOpen = showHitlTask != null ||
                    selectedTaskInspector != null ||
                    showDelegationModal ||
                    selectedAgentDetail != null ||
                    showOnboardingWizard

                BackHandler(enabled = modalOpen) {
                    when {
                        showHitlTask != null -> viewModel.closeHitlDecision()
                        selectedTaskInspector != null -> viewModel.closeTaskInspector()
                        showDelegationModal -> viewModel.closeDelegationModal()
                        selectedAgentDetail != null -> viewModel.closeAgentDetail()
                        showOnboardingWizard -> viewModel.closeOnboardingWizard()
                    }
                }

                // Handle Toast alerts
                LaunchedEffect(toastMsg) {
                    toastMsg?.let { msg ->
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                Scaffold(
                    topBar = {
                        GlobalHeader(
                            activeTasks = activeTasks,
                            hitlCount = hitlCount,
                            dailyBurnUSD = dailyBurn,
                            isKillSwitchEngaged = isKillSwitchEngaged,
                            onKillSwitchClick = { viewModel.setTab(3) }, // Switch to Settings for Kill Switch
                            onHitlAlertClick = { viewModel.setTab(1) } // Switch to Tasks tab
                        )
                    },
                    bottomBar = {
                        AgentOSBottomNavigation(
                            selectedTab = activeTab,
                            onTabSelected = { viewModel.setTab(it) },
                            hitlBadgeCount = hitlCount
                        )
                    },
                    contentWindowInsets = WindowInsets.safeDrawing,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 840.dp)
                                .fillMaxWidth()
                                .fillMaxHeight(),
                        ) {
                        when (activeTab) {
                            0 -> AgentsScreen(viewModel = viewModel)
                            1 -> TasksScreen(viewModel = viewModel)
                            2 -> AnalyticsScreen(viewModel = viewModel)
                            3 -> SettingsScreen(viewModel = viewModel)
                            else -> AgentsScreen(viewModel = viewModel)
                        }

                        // Onboarding Wizard Modal
                        if (showOnboardingWizard) {
                            AgentOnboardingModal(
                                viewModel = viewModel,
                                onDismiss = { viewModel.closeOnboardingWizard() }
                            )
                        }

                        // Agent Detail Sheet
                        selectedAgentDetail?.let { agent ->
                            AgentDetailModal(
                                agent = agent,
                                onDelegateClick = { viewModel.openDelegationModal(agent.id) },
                                onDeleteClick = { viewModel.deleteAgent(agent.id) },
                                onDismiss = { viewModel.closeAgentDetail() }
                            )
                        }

                        // Task Delegation Modal
                        if (showDelegationModal) {
                            TaskDelegationModal(
                                viewModel = viewModel,
                                agents = agents,
                                onDismiss = { viewModel.closeDelegationModal() }
                            )
                        }

                        // Task Inspector Modal
                        selectedTaskInspector?.let { task ->
                            TaskInspectorModal(
                                task = task,
                                onApproveHitl = { viewModel.approveHitl(task.id) },
                                onRejectHitl = { viewModel.rejectHitl(task.id) },
                                onDismiss = { viewModel.closeTaskInspector() }
                            )
                        }

                        // HITL Deep Decision Sheet
                        showHitlTask?.let { task ->
                            HitlDecisionSheet(
                                task = task,
                                onApprove = { viewModel.approveHitl(task.id) },
                                onReject = { viewModel.rejectHitl(task.id) },
                                onDismiss = { viewModel.closeHitlDecision() }
                            )
                        }
                        }
                    }
                }
            }
        }
    }
}
