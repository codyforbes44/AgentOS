package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskExecutionEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskInspectorModal(
    task: TaskExecutionEntity,
    onApproveHitl: () -> Unit,
    onRejectHitl: () -> Unit,
    onDismiss: () -> Unit
) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Task #${task.id} • ${task.agentName}",
                        color = CyanAccent,
                        fontSize = 12.sp
                    )
                }
                StatusBadge(status = task.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Node Graph Visualizer
            Text(text = "Execution Node Graph (${task.flowType})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (step in 1..task.totalSteps) {
                    val isDone = step < task.currentStepIndex || task.status == "COMPLETED"
                    val isCurrent = step == task.currentStepIndex && task.status != "COMPLETED"

                    val nodeColor = when {
                        isDone -> StatusOnline
                        isCurrent && task.status == "AWAITING_HITL" -> StatusApprovalRequired
                        isCurrent && (task.status == "RUNNING" || task.status == "EXECUTING") -> StatusExecuting
                        isCurrent && (task.status == "FAILED" || task.status == "KILLED") -> StatusError
                        else -> SurfaceVariantDark
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(nodeColor.copy(alpha = 0.2f))
                            .border(1.dp, nodeColor, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Step $step",
                            color = nodeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    if (step < task.totalSteps) {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next", tint = TextMuted, modifier = Modifier.size(12.dp))
                    }
                }
            }

            // HITL Callout Banner if AWAITING_HITL
            if (task.status == "AWAITING_HITL") {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = StatusApprovalRequired.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusApprovalRequired),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Gavel, contentDescription = "HITL Gate", tint = StatusApprovalRequired)
                            Text(text = "Human-in-the-Loop Decision Sheet", color = StatusApprovalRequired, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = task.hitlReason, color = TextPrimary, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRejectHitl,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StatusError),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Text("Reject", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onApproveHitl,
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusOnline),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Text("Approve", color = BackgroundDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Log Console Stream Box
            Text(text = "Execution Log Stream (Real-Time)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = BackgroundDark,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = task.logsText,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text("Close Inspector", color = TextPrimary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
