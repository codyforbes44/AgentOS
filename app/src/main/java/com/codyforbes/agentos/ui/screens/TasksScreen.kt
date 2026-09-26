package com.codyforbes.agentos.ui.screens

import com.codyforbes.agentos.R

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codyforbes.agentos.data.TaskExecutionEntity
import com.codyforbes.agentos.ui.components.TaskCard
import com.codyforbes.agentos.ui.theme.*
import com.codyforbes.agentos.viewmodel.AgentOSViewModel

@Composable
fun TasksScreen(
    viewModel: AgentOSViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val taskFilter by viewModel.taskStatusFilter.collectAsStateWithLifecycle()

    val filteredTasks = remember(tasks, taskFilter) {
        tasks.filter { t ->
            when (taskFilter) {
                "ACTIVE" -> t.status == "RUNNING" || t.status == "AWAITING_HITL" || t.status == "QUEUED"
                "AWAITING_HITL" -> t.status == "AWAITING_HITL"
                "COMPLETED" -> t.status == "COMPLETED"
                "FAILED" -> t.status == "FAILED" || t.status == "KILLED"
                else -> true
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
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
                    text = stringResource(R.string.tasks_title),
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = (-1.0).sp
                )
                Text(
                    text = stringResource(
                        R.string.tasks_active_count,
                        tasks.count { it.status == "RUNNING" || it.status == "AWAITING_HITL" },
                    ),
                    color = TextSecondary,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp
                )
            }

            Button(
                onClick = { viewModel.openDelegationModal() },
                colors = ButtonDefaults.buttonColors(containerColor = StatusExecuting),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("delegate_task_button")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(R.string.tasks_delegate), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 0.5.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Task Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filters = listOf("ALL", "ACTIVE", "AWAITING_HITL", "COMPLETED", "FAILED")
            items(filters, key = { it }) { filter ->
                val isSel = taskFilter == filter
                val isHitlTab = filter == "AWAITING_HITL"
                val hitlCount = tasks.count { it.status == "AWAITING_HITL" }

                Surface(
                    color = if (isSel) (if (isHitlTab) StatusApprovalRequired else StatusExecuting) else SurfaceDark,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) (if (isHitlTab) StatusApprovalRequired else StatusExecuting) else SurfaceBorderDark),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable { viewModel.setTaskStatusFilter(filter) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (filter == "AWAITING_HITL") stringResource(R.string.filter_hitl_gates) else filter,
                            color = if (isSel) (if (isHitlTab) OnStatusFill else TextPrimary) else TextSecondary,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                        if (isHitlTab && hitlCount > 0) {
                            Badge(containerColor = StatusApprovalRequired) {
                                Text(text = "$hitlCount", color = OnStatusFill, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.tasks_empty),
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onClick = { viewModel.openTaskInspector(task) },
                        onHitlClick = { viewModel.openHitlDecision(task) }
                    )
                }
            }
        }
    }
}
