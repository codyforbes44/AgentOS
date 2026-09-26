package com.codyforbes.agentos

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.codyforbes.agentos.data.AgentOSDatabase
import com.codyforbes.agentos.data.AgentOSRepository
import com.codyforbes.agentos.ui.components.AgentOSBottomNavigation
import com.codyforbes.agentos.ui.components.GlobalHeader
import com.codyforbes.agentos.ui.components.HitlDecisionSheet
import com.codyforbes.agentos.ui.screens.*
import com.codyforbes.agentos.ui.theme.AgentOSTheme
import com.codyforbes.agentos.viewmodel.AgentOSViewModel
import com.codyforbes.agentos.viewmodel.AgentOSViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: AgentOSViewModel by viewModels {
        val db = AgentOSDatabase.getDatabase(applicationContext)
        val repository = AgentOSRepository(db, kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO))
        AgentOSViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AgentOSTheme {
                val activeTab by viewModel.activeTab.collectAsState()
                val activeTasks by viewModel.activeTaskCount.collectAsState()
                val hitlCount by viewModel.awaitingHitlCount.collectAsState()
                val dailyBurn by viewModel.totalDailyBurnUSD.collectAsState()
                val settings by viewModel.settings.collectAsState()
                val toastMsg by viewModel.toastMessage.collectAsState()

                val agents by viewModel.agents.collectAsState()
                val selectedAgentDetail by viewModel.selectedAgentDetail.collectAsState()
                val selectedTaskInspector by viewModel.selectedTaskInspector.collectAsState()
                val showOnboardingWizard by viewModel.showOnboardingWizard.collectAsState()
                val showDelegationModal by viewModel.showDelegationModal.collectAsState()
                val showHitlTask by viewModel.showHitlDecisionTask.collectAsState()

                val isKillSwitchEngaged = settings?.emergencyKillSwitchEngaged == true

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
                            .padding(innerPadding)
                    ) {
                        when (activeTab) {
                            0 -> AgentsScreen(viewModel = viewModel)
                            1 -> TasksScreen(viewModel = viewModel)
                            2 -> AnalyticsScreen(viewModel = viewModel)
                            3 -> SettingsScreen(viewModel = viewModel)
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
