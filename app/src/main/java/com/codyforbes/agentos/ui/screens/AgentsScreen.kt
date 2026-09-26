package com.codyforbes.agentos.ui.screens

import com.codyforbes.agentos.R

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codyforbes.agentos.data.AgentEntity
import com.codyforbes.agentos.ui.components.AgentCard
import com.codyforbes.agentos.ui.components.EmptyState
import com.codyforbes.agentos.ui.theme.*
import com.codyforbes.agentos.viewmodel.AgentOSViewModel

@Composable
fun AgentsScreen(
    viewModel: AgentOSViewModel,
    modifier: Modifier = Modifier
) {
    val agents by viewModel.agents.collectAsStateWithLifecycle()
    val statusFilter by viewModel.agentStatusFilter.collectAsStateWithLifecycle()
    val providerFilter by viewModel.agentProviderFilter.collectAsStateWithLifecycle()

    val filteredAgents = remember(agents, statusFilter, providerFilter) {
        agents.filter { ag ->
            val matchStatus = statusFilter == "ALL" || ag.status.equals(statusFilter, ignoreCase = true)
            val matchProvider = providerFilter == "ALL" || ag.provider.equals(providerFilter, ignoreCase = true)
            matchStatus && matchProvider
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.agents_title),
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = (-1.0).sp
                    )
                    Text(
                        text = stringResource(R.string.agents_connected_count, agents.size),
                        color = TextSecondary,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                }

                Button(
                    onClick = { viewModel.openOnboardingWizard() },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusExecuting),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("connect_agent_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.agents_connect), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Status Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val statusFilters = listOf("ALL", "ONLINE_IDLE", "EXECUTING", "AWAITING_APPROVAL", "RATE_LIMITED", "ERROR")
                items(statusFilters, key = { it }) { filter ->
                    val isSel = statusFilter == filter
                    Surface(
                        color = if (isSel) StatusExecuting else SurfaceDark,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) StatusExecuting else SurfaceBorderDark),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clickable { viewModel.setAgentStatusFilter(filter) }
                    ) {
                        Text(
                            text = filter.replace("_", " "),
                            color = if (isSel) TextPrimary else TextSecondary,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredAgents.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.empty_agents_title),
                    description = stringResource(R.string.empty_agents_description),
                    actionLabel = stringResource(R.string.empty_agents_action),
                    onActionClick = { viewModel.openOnboardingWizard() },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredAgents, key = { it.id }) { agent ->
                        AgentCard(
                            agent = agent,
                            onClick = { viewModel.openAgentDetail(agent) },
                            onDelegateClick = { viewModel.openDelegationModal(agent.id) }
                        )
                    }
                }
            }
        }

        // Material3 Floating Action Button for Connecting Agent
        ExtendedFloatingActionButton(
            onClick = { viewModel.openOnboardingWizard() },
            containerColor = StatusExecuting,
            contentColor = TextPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 16.dp)
                .testTag("fab_connect_agent")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = TextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.agents_connect_fab),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp,
                color = TextPrimary
            )
        }
    }
}
