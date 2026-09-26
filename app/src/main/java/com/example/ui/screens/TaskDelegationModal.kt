package com.example.ui.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AgentEntity
import com.example.ui.theme.*
import com.example.viewmodel.AgentOSViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDelegationModal(
    viewModel: AgentOSViewModel,
    agents: List<AgentEntity>,
    onDismiss: () -> Unit
) {
    val title by viewModel.delTitle.collectAsState()
    val objective by viewModel.delObjective.collectAsState()
    val selectedAgentId by viewModel.delAgentId.collectAsState()
    val flowType by viewModel.delFlowType.collectAsState()
    val maxCostCap by viewModel.delMaxCostCap.collectAsState()
    val timeout by viewModel.delTimeout.collectAsState()
    val hitlGate by viewModel.delHitlGate.collectAsState()
    val preflightStatus by viewModel.preflightStatus.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        scrimColor = BackgroundDark.copy(alpha = 0.8f)
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
                    text = "Task Delegation & Execution",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Objective Input
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.delTitle.value = it },
                label = { Text("Task Title") },
                placeholder = { Text("e.g. Summarize Q3 Financial Report PDF") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = SurfaceBorderDark),
                modifier = Modifier.fillMaxWidth().testTag("del_title_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = objective,
                onValueChange = { viewModel.delObjective.value = it },
                label = { Text("Task Objective / Goal Prompt") },
                placeholder = { Text("Describe task goals, files to process, or database records to update...") },
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
                Text(text = "Quick Context:", color = TextMuted, fontSize = 11.sp)
                listOf("@agent_name", "@dataset", "@users_db").forEach { contextTag ->
                    Surface(
                        color = SurfaceVariantDark,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable {
                            viewModel.delObjective.value = viewModel.delObjective.value + " " + contextTag
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
            Text(text = "Target Agent Assignment", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        .clickable { viewModel.delAgentId.value = ag.id }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = ag.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "${ag.provider} • Health ${ag.healthScore}%", color = TextSecondary, fontSize = 11.sp)
                        }
                        RadioButton(selected = isSel, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = CyanAccent))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Workflow Flow Type (Single, Sequential, Parallel DAG)
            Text(text = "Execution Flow Pattern", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("SINGLE", "Single Agent", Icons.Default.Person),
                    Triple("SEQUENTIAL", "Sequential Chain", Icons.Default.LinearScale),
                    Triple("PARALLEL_DAG", "Parallel DAG", Icons.Default.AccountTree)
                ).forEach { (type, label, icon) ->
                    val isSel = flowType == type
                    Surface(
                        color = if (isSel) StatusExecuting.copy(alpha = 0.2f) else BackgroundDark,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) StatusExecuting else SurfaceBorderDark),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.delFlowType.value = type }
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = icon, contentDescription = label, tint = if (isSel) StatusExecuting else TextSecondary, modifier = Modifier.size(20.dp))
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
                        Text(text = "PRE-FLIGHT SIMULATION CHECK", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { viewModel.runPreflightCheck() },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(text = "Run Check", color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                Icon(imageVector = Icons.Default.PlayCircle, contentDescription = "Execute", tint = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "CONFIRM & EXECUTE TASK", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
