package com.codyforbes.agentos.ui.screens

import com.codyforbes.agentos.R

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codyforbes.agentos.data.AgentEntity
import com.codyforbes.agentos.ui.theme.*
import com.codyforbes.agentos.viewmodel.AgentOSViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDelegationModal(
    viewModel: AgentOSViewModel,
    agents: List<AgentEntity>,
    onDismiss: () -> Unit
) {
    val title by viewModel.delTitle.collectAsStateWithLifecycle()
    val objective by viewModel.delObjective.collectAsStateWithLifecycle()
    val selectedAgentId by viewModel.delAgentId.collectAsStateWithLifecycle()
    val flowType by viewModel.delFlowType.collectAsStateWithLifecycle()
    val maxCostCap by viewModel.delMaxCostCap.collectAsStateWithLifecycle()
    val timeout by viewModel.delTimeout.collectAsStateWithLifecycle()
    val hitlGate by viewModel.delHitlGate.collectAsStateWithLifecycle()
    val preflightStatus by viewModel.preflightStatus.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.delegate_title),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.cd_close), tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Objective Input
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.onDelTitleChange(it) },
                label = { Text(stringResource(R.string.delegate_task_title)) },
                placeholder = { Text(stringResource(R.string.delegate_title_placeholder)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = SurfaceBorderDark),
                modifier = Modifier.fillMaxWidth().testTag("del_title_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = objective,
                onValueChange = { viewModel.onDelObjectiveChange(it) },
                label = { Text(stringResource(R.string.delegate_objective_label)) },
                placeholder = { Text(stringResource(R.string.delegate_objective_placeholder)) },
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = SurfaceBorderDark),
                modifier = Modifier.fillMaxWidth().testTag("del_objective_input")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Context insertion tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.delegate_quick_context), color = TextMuted, fontSize = 11.sp)
                listOf("@agent_name", "@dataset", "@users_db").forEach { contextTag ->
                    Surface(
                        color = SurfaceVariantDark,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable {
                            viewModel.appendDelObjective(contextTag)
                        }
                    ) {
                        Text(
                            text = contextTag,
                            color = CyanAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Agent Selector
            Text(text = stringResource(R.string.delegate_target), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            agents.forEach { ag ->
                val isSel = selectedAgentId == ag.id
                Surface(
                    color = if (isSel) SurfaceVariantDark else BackgroundDark,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) CyanAccent else SurfaceBorderDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .defaultMinSize(minHeight = 48.dp)
                        .clickable { viewModel.onDelAgentIdChange(ag.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = ag.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = stringResource(R.string.delegate_agent_meta, ag.provider, ag.healthScore), color = TextSecondary, fontSize = 11.sp)
                        }
                        RadioButton(selected = isSel, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = CyanAccent))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Workflow Flow Type (Single, Sequential, Parallel DAG)
            Text(text = stringResource(R.string.delegate_flow), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("SINGLE", stringResource(R.string.flow_single), Icons.Default.Person),
                    Triple("SEQUENTIAL", stringResource(R.string.flow_sequential), Icons.Default.LinearScale),
                    Triple("PARALLEL_DAG", stringResource(R.string.flow_dag), Icons.Default.AccountTree)
                ).forEach { (type, label, icon) ->
                    val isSel = flowType == type
                    Surface(
                        color = if (isSel) StatusExecuting.copy(alpha = 0.2f) else BackgroundDark,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) StatusExecuting else SurfaceBorderDark),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .clickable { viewModel.onDelFlowTypeChange(type) }
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = if (isSel) StatusExecuting else TextSecondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = label, color = if (isSel) TextPrimary else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pre-flight Check Display Sheet
            Surface(
                color = BackgroundDark,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.delegate_preflight), color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { viewModel.runPreflightCheck() },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(text = stringResource(R.string.delegate_run_check), color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = preflightStatus ?: "[ PRE-FLIGHT CHECK: PASSED ] Estimated Cost: ~$0.12 USD | Est. Latency: 18s",
                        color = StatusOnline,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Confirm & Execute Task Button
            Button(
                onClick = { viewModel.confirmExecuteTask() },
                colors = ButtonDefaults.buttonColors(containerColor = StatusExecuting),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("confirm_execute_task_button")
            ) {
                Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.delegate_confirm), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
